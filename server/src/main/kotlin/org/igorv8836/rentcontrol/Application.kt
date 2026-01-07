package org.igorv8836.rentcontrol

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.igorv8836.bdui.backend.core.BackendResult
import org.igorv8836.bdui.backend.runtime.engine.AnnotationRegistrar
import org.igorv8836.bdui.backend.runtime.engine.BackendRegistry
import org.igorv8836.bdui.backend.runtime.engine.Engine
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.rentcontrol.backend.v2.HomeParams
import org.igorv8836.rentcontrol.backend.v2.CatalogParams
import org.igorv8836.rentcontrol.backend.v2.DetailsParams

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val registry = BackendRegistry().also {
        AnnotationRegistrar.registerAll(
            registry = it,
            packages = listOf("org.igorv8836.rentcontrol.backend.v2"),
        )
    }
    val engine = Engine(registry)
    val mapper = jacksonObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        route("/bdui") {
            get("/home") {
                call.respondScreen(mapper, engine.render(HomeParams))
            }
            get("/catalog") {
                call.respondScreen(mapper, engine.render(CatalogParams))
            }
            get("/details/{id}") {
                val id = call.parameters["id"].orEmpty()
                call.respondScreen(mapper, engine.render(DetailsParams(id)))
            }
        }
    }
}

private suspend fun ApplicationCall.respondScreen(
    mapper: ObjectMapper,
    result: BackendResult<RemoteScreen>,
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
