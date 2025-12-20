package org.igorv8836.rentcontrol

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude
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
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        route("/bdui") {
            get("/home") {
                call.respondScreen(mapper, engine.render(screenId = "home", input = Unit))
            }
            get("/catalog") {
                call.respondScreen(mapper, engine.render(screenId = "catalog", input = Unit))
            }
            get("/details/{id}") {
                val id = call.parameters["id"].orEmpty()
                call.respondScreen(mapper, engine.render(screenId = "details", input = id))
            }
        }
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.respondScreen(
    mapper: com.fasterxml.jackson.databind.ObjectMapper,
    result: BackendResult<org.igorv8836.bdui.contract.RemoteScreen>,
) {
    when (result) {
        is BackendResult.Success -> {
            val json = mapper.writeValueAsString(result.value)
            respondText(json, ContentType.Application.Json)
        }

        is BackendResult.Failure -> {
            val json = mapper.writeValueAsString(result.error)
            respondText(json, status = HttpStatusCode.BadRequest, contentType = ContentType.Application.Json)
        }
    }
}
