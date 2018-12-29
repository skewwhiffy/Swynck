package swynck.app

import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters.Cors
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import swynck.app.api.Api

class App(dependencies: Dependencies) : RoutingHttpHandler by cors.then(routes(
    "/ping" bind GET to { Response(OK).body("pong") },
    "/api" bind Api(dependencies),
    static(Classpath("www"))
)) {
    companion object {
        private val policy = CorsPolicy(listOf("*"), listOf(), Method.values().toList())
        val cors = Cors(policy)
    }
}
