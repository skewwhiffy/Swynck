package swynck.app

import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
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
        oneDrive: Onedrive
    ) = routes(
        "/ping" bind GET to { Response(OK).body("pong") },
        "/user/me" bind GET to { GetCurrentUser(userRepository, oneDrive) }
    )
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
                else -> Response(OK).withBody(User(it.name))
            }
        }
}

abstract class UserResponse

data class User(
    val name: String
) : UserResponse() {
    companion object {
        val lens = Body.auto<User>().toLens()
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
    is User -> User.lens.inject(userResponse, this)
    is UserNotFound -> UserNotFound.lens.inject(userResponse, this)
    else -> throw NotImplementedError()
}
