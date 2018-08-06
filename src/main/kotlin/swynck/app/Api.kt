package swynck.app

import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import swynck.config.Json.auto
import swynck.db.UserRepository

object Api {
    operator fun invoke(userRepository: UserRepository) = routes(
        "/ping" bind GET to { Response(OK).body("pong") },
        "/user/me" bind GET to { GetCurrentUser(userRepository) }
    )
}

object GetCurrentUser {
    operator fun invoke(userRepository: UserRepository) = userRepository
        .getUser()
        .let {
            when (it) {
                null -> Response(NOT_FOUND)
                else -> Response(OK).withBody(User(it.name))
            }
        }
}

data class User(
    val name: String
) {
    companion object {
        val lens = Body.auto<User>().toLens()
    }
}

private fun Response.withBody(user: User) = User.lens.inject(user, this)