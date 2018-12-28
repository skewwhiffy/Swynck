package swynck.test.utils

import org.assertj.core.api.ObjectAssert
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.routing.RoutingHttpHandler

fun <T: RoutingHttpHandler> ObjectAssert<T>.hasPingEndpoint(route: String = "/ping") {
    matches {
        val result = it(Request(GET, route))
        result.status == Status.OK && result.bodyString() == "pong"
    }
}