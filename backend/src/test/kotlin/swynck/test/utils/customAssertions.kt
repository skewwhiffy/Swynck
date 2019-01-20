package swynck.test.utils

import org.assertj.core.api.ObjectAssert
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status

fun <T: HttpHandler> ObjectAssert<T>.hasPingEndpoint(route: String = "/ping") {
    matches {
        val result = it(Request(GET, route))
        result.status == Status.OK && result.bodyString() == "pong"
    }
}