package swynck.app

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static

object App {
    private val rewriteUriToSlash = Filter { next: HttpHandler -> {
        it: Request -> next(it.uri(it.uri.path("/")))
    }}
    operator fun invoke() = routes(
        "/ping" bind GET to { Response(OK).body("pong") },
        static(Classpath("www")),
        rewriteUriToSlash.then(static(Classpath("www")))
    )
}