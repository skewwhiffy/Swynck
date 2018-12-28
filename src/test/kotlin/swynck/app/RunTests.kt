package swynck.app

import assertk.assert
import assertk.assertions.isEqualTo
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.junit.After
import org.junit.Test
import swynck.test.utils.StartServerForTesting

class RunTests {
    private val server = StartServerForTesting()

    @After
    fun tearDown() = server.close()

    @Test
    fun `ping endpoint works`() {
        val response = server.client(Request(GET, "http://localhost:${server.dependencies.config.port()}/ping"))

        assert(response.bodyString()).isEqualTo("pong")
    }
}