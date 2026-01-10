package org.igorv8836.rentcontrol.server.modules.me.module

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.igorv8836.rentcontrol.server.foundation.security.userContext
import org.igorv8836.rentcontrol.server.modules.me.api.dto.MeResponse
import org.igorv8836.rentcontrol.server.modules.me.api.dto.StatusResponse
import org.igorv8836.rentcontrol.server.modules.me.api.dto.UpdateMeRequest
import org.igorv8836.rentcontrol.server.modules.me.domain.service.MeService

fun Route.meModule(meService: MeService) {
    route("/me") {
        get {
            val user = meService.getMe(call.userContext.userId)
            call.respond(user.toMeResponse())
        }

        patch {
            val request = call.receive<UpdateMeRequest>()
            val user = meService.updateMe(
                userId = call.userContext.userId,
                fullName = request.fullName,
                phone = request.phone,
                preferences = request.preferences,
            )
            call.respond(user.toMeResponse())
        }

        route("/sessions") {
            post("/logout-all") {
                meService.logoutAll(call.userContext.userId)
                call.respond(StatusResponse(status = "OK"))
            }
        }
    }
}

private fun org.igorv8836.rentcontrol.server.modules.users.domain.model.User.toMeResponse(): MeResponse =
    MeResponse(
        id = id,
        email = email,
        fullName = fullName,
        phone = phone,
        role = role,
        status = status,
        preferences = preferences,
    )

