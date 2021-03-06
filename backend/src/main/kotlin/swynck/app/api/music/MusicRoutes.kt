package swynck.app.api.music

import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import swynck.app.Dependencies

class MusicRoutes(dependencies: Dependencies): RoutingHttpHandler by routes(
    "/ping" bind GET to { Response(OK).body("pong") },
    "/" bind GET to { GetMusic(dependencies, it) }
)