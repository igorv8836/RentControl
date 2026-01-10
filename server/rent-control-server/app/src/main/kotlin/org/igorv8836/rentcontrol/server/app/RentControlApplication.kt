package org.igorv8836.rentcontrol.server.app

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.igorv8836.rentcontrol.server.foundation.db.DatabaseFactory
import org.igorv8836.rentcontrol.server.foundation.errors.installErrorHandling
import org.igorv8836.rentcontrol.server.foundation.http.installHttpBasics
import org.igorv8836.rentcontrol.server.foundation.security.BearerAuth
import org.igorv8836.rentcontrol.server.integrations.otp.MockOtpSender
import org.igorv8836.rentcontrol.server.modules.auth.data.repo.ExposedAuthSessionRepository
import org.igorv8836.rentcontrol.server.modules.auth.data.repo.ExposedOtpRepository
import org.igorv8836.rentcontrol.server.modules.auth.domain.service.AuthService
import org.igorv8836.rentcontrol.server.modules.auth.domain.service.PasswordHasher
import org.igorv8836.rentcontrol.server.modules.auth.domain.service.TokenService
import org.igorv8836.rentcontrol.server.modules.auth.module.authModule
import org.igorv8836.rentcontrol.server.modules.me.domain.service.MeService
import org.igorv8836.rentcontrol.server.modules.me.module.meModule
import org.igorv8836.rentcontrol.server.modules.objects.data.repo.ExposedObjectsRepository
import org.igorv8836.rentcontrol.server.modules.objects.domain.service.ObjectsService
import org.igorv8836.rentcontrol.server.modules.objects.module.objectsModule
import org.igorv8836.rentcontrol.server.modules.users.data.repo.ExposedUsersRepository

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    installHttpBasics()
    installErrorHandling()

    val dataSource = DatabaseFactory.createDataSource(environment)
    val database = DatabaseFactory.connectAndMigrate(dataSource)

    environment.monitor.subscribe(ApplicationStopped) {
        dataSource.close()
    }

    val usersRepository = ExposedUsersRepository(database)
    val sessionsRepository = ExposedAuthSessionRepository(database)
    val otpRepository = ExposedOtpRepository(database)
    val objectsRepository = ExposedObjectsRepository(database)

    val passwordHasher = PasswordHasher()
    val tokenService = TokenService()
    val otpSender = MockOtpSender(environment.log)

    val authService = AuthService(
        usersRepository = usersRepository,
        sessionsRepository = sessionsRepository,
        otpRepository = otpRepository,
        passwordHasher = passwordHasher,
        tokenService = tokenService,
        otpSender = otpSender,
    )

    val meService = MeService(
        usersRepository = usersRepository,
        sessionsRepository = sessionsRepository,
    )

    val objectsService = ObjectsService(
        objectsRepository = objectsRepository,
        usersRepository = usersRepository,
    )

    install(BearerAuth) {
        this.sessionsRepository = sessionsRepository
    }

    routing {
        get("/health") {
            call.respondText("ok")
        }
        get("/health/db") {
            val dbStatus = runCatching { DatabaseFactory.checkDatabase(database) }

            if (dbStatus.isSuccess) {
                call.respondText("ok")
            } else {
                dbStatus.exceptionOrNull()?.let { cause ->
                    environment.log.warn("Database health check failed", cause)
                }
                call.respondText(
                    text = "database unavailable",
                    status = HttpStatusCode.ServiceUnavailable,
                )
            }
        }

        route("/api/v1") {
            authModule(authService)
            meModule(meService)
            objectsModule(objectsService)
        }
    }
}
