package swynck.app.api

import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.lens.Path
import org.http4k.lens.string
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import swynck.app.Dependencies
import swynck.app.api.items.ItemsRoutes
import swynck.app.api.music.MusicRoutes

class Api(dependencies: Dependencies): RoutingHttpHandler by routes(
    "/ping" bind GET to { Response(OK).body("pong") },
    "/user/me" bind GET to { GetCurrentUser(dependencies) },
    "/onedrive/authcode" bind POST to { OnedriveCallback(dependencies, it) },
    "/items" bind GET to { ItemsRoutes(dependencies)(listOf()) },
    "/items/{any:.*}" bind GET to { req ->
        itemsLens(req)
            .split("/")
            .filter { ! it.isBlank() }
            .let { it -> ItemsRoutes(dependencies)(it) }
    },
       // ItemsRoutes(dependencies),
    "/music" bind MusicRoutes(dependencies)
) {
    companion object {
        private val itemsLens = Path.string().of("any")
    }
}
