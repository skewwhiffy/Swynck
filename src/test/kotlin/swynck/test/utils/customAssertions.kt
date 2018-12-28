package swynck.test.utils

import assertk.Assert
import assertk.assertions.*
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.routing.RoutingHttpHandler

fun <T> Assert<T>.satisfies(condition: (T) -> Boolean) = actual
    .let(condition)
    .let { assert(it) }
    .isEqualTo(true)

fun Assert<String>.isNotBlank() = satisfies { it.isNotBlank() }

fun Assert<RoutingHttpHandler>.hasPingEndpoint(route: String = "/ping") {
    val result = actual(Request(Method.GET, route))
    assert(result).satisfies { it.status == Status.OK }
    assert(result).satisfies { it.bodyString() == "pong" }
}