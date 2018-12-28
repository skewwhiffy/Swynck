package swynck.app

import assertk.assert
import assertk.assertions.*
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.junit.Test
import swynck.test.utils.TestDependencies
import swynck.test.utils.satisfies

class AppTests {
    private val dependencies = TestDependencies()
    private val app = App(dependencies)

    @Test
    fun `app ping endpoint responds`() {
        val response = app(Request(GET, "/ping"))

        assert(response).satisfies { it.status.successful }
        assert(response.bodyString()).isEqualTo("pong")
    }

    @Test
    fun `api is correctly wired in`() {
        val dependencies = TestDependencies()
        val app = App(dependencies)

        val response = app(Request(GET, "/api/ping"))

        assert(response).satisfies { it.status.successful }
        assert(response.bodyString()).isEqualTo("pong")
    }
}