package org.igorv8836.rentcontrol

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.runtime.BackendEngine
import org.igorv8836.rentcontrol.backend.module.DemoBackendModule

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val engine = BackendEngine.create(
        autoLoadModules = true,
        modules = listOf(DemoBackendModule()),
    )
    val mapper = jacksonObjectMapper()

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        get("/bdui/home") {
            val result = engine.render(screenId = "home", input = Unit)
            when (result) {
                is BackendResult.Success -> {
                    val json = mapper.writeValueAsString(result.value)
                    call.respondText(json, ContentType.Application.Json)
                }

                is BackendResult.Failure -> {
                    val json = mapper.writeValueAsString(result.error)
                    call.respondText(json, status = HttpStatusCode.BadRequest, contentType = ContentType.Application.Json)
                }
            }
        }
    }
}
