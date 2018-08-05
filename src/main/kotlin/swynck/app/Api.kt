package swynck.app

import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes

object Api {
    operator fun invoke() = routes(
        "/ping" bind GET to { Response(OK).body("pong") }
    )
}