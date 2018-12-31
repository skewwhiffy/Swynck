package swynck.app.api

import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import swynck.app.Dependencies
import swynck.common.Json.auto
import java.net.URI

object GetCurrentUser {
    operator fun invoke(
        dependencies: Dependencies
    ) = dependencies
        .userRepository
        .getUser()
        .let {
            when (it) {
                null -> Response(OK).withBody(UserNotFound(dependencies.oneDrive.authenticationUrl()))
                else -> Response(OK).withBody(UserFound(it.displayName))
            }
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
