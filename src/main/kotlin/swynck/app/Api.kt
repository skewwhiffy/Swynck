package swynck.app

import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.ACCEPTED
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import swynck.config.Json.auto
import swynck.db.UserRepository
import swynck.service.Onedrive
import java.net.URI

object Api {
    operator fun invoke(
        userRepository: UserRepository,
        onedrive: Onedrive
    ) = routes(
        "/ping" bind GET to { Response(OK).body("pong") },
        "/user/me" bind GET to { GetCurrentUser(userRepository, onedrive) },
        "/onedrive/authcode" bind POST to { OnedriveCallback(onedrive, userRepository, it) },
        "/files" bind GET to { GetFiles() }
    )
}

object GetFiles {
    operator fun invoke(): Response {
        return Response(OK).body("[]")
    }
}

object OnedriveCallback {
    operator fun invoke(
        onedrive: Onedrive,
        userRepository: UserRepository,
        request: Request
    ): Response {
        val requestDeserialized = OnedriveCallbackRequest(request)
        val accessToken = onedrive.getAccessToken(requestDeserialized.authCode)
        val userDetails = onedrive.getUser(accessToken)
        userRepository.addUser(userDetails)
        return Response(ACCEPTED)
    }
}

object GetCurrentUser {
    operator fun invoke(
        userRepository: UserRepository,
        oneDrive: Onedrive
    ) = userRepository
        .getUser()
        .let {
            when (it) {
                null -> Response(OK).withBody(UserNotFound(oneDrive.authenticationUrl()))
                else -> Response(OK).withBody(UserFound(it.displayName))
            }
        }
}

data class OnedriveCallbackRequest(
    val authCode: String
) {
    companion object {
        private val lens = Body.auto<OnedriveCallbackRequest>().toLens()
        operator fun invoke(request: Request) = lens(request)
    }
}

abstract class UserResponse

data class UserFound(
    val displayName: String
) : UserResponse() {
    companion object {
        val lens = Body.auto<UserFound>().toLens()
    }
}

data class UserNotFound(
    val redirect: URI
) : UserResponse() {
    companion object {
        val lens = Body.auto<UserNotFound>().toLens()
    }
}

private fun Response.withBody(userResponse: UserResponse) = when(userResponse) {
    is UserFound -> UserFound.lens.inject(userResponse, this)
    is UserNotFound -> UserNotFound.lens.inject(userResponse, this)
    else -> throw NotImplementedError()
}
