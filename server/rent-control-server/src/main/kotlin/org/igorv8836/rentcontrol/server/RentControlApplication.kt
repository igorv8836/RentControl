package org.igorv8836.rentcontrol.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.netty.EngineMain
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val dataSource = DatabaseConfig.from(environment).buildDataSource()
    val database = initDatabase(dataSource)

    environment.monitor.subscribe(ApplicationStopped) {
        dataSource.close()
    }

    routing {
        get("/health") {
            call.respondText("ok")
        }
        get("/health/db") {
            val dbStatus = runCatching {
                transaction(database) {
                    exec("SELECT 1") { result -> result.next() }
                }
            }

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
    }
}

private fun initDatabase(dataSource: DataSource): Database {
    val database = Database.connect(dataSource)
    transaction(database) {
        SchemaUtils.createMissingTablesAndColumns(*allTables)
    }
    return database
}

private data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maxPoolSize: Int,
) {
    fun buildDataSource(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = this@DatabaseConfig.jdbcUrl
            username = this@DatabaseConfig.username
            password = this@DatabaseConfig.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = maxPoolSize
        }
        return HikariDataSource(config)
    }

    companion object {
        private const val DEFAULT_URL = "jdbc:postgresql://localhost:5432/rent_control"
        private const val DEFAULT_USERNAME = "rent_control"
        private const val DEFAULT_PASSWORD = "rent_control"
        private const val DEFAULT_POOL_SIZE = 5

        fun from(environment: ApplicationEnvironment): DatabaseConfig {
            fun property(path: String, env: String, default: String): String {
                val fromEnv = System.getenv(env)
                val fromConfig = runCatching {
                    environment.config.propertyOrNull(path)?.getString()
                }.getOrNull()
                return listOf(fromEnv, fromConfig, default)
                    .firstOrNull { it.orEmpty().isNotBlank() }
                    ?: default
            }

            fun intProperty(path: String, env: String, default: Int): Int {
                val raw = property(path, env, default.toString())
                return raw.toIntOrNull() ?: default
            }

            return DatabaseConfig(
                jdbcUrl = property("database.url", "RENT_CONTROL_DB_URL", DEFAULT_URL),
                username = property("database.user", "RENT_CONTROL_DB_USER", DEFAULT_USERNAME),
                password = property("database.password", "RENT_CONTROL_DB_PASSWORD", DEFAULT_PASSWORD),
                maxPoolSize = intProperty("database.maxPoolSize", "RENT_CONTROL_DB_POOL_SIZE", DEFAULT_POOL_SIZE),
            )
        }
    }
}
