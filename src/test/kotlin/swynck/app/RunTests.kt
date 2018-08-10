package swynck.app

import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import swynck.test.utils.StartServerForTesting

class RunTests {
    companion object {
        private lateinit var server: StartServerForTesting

        @JvmStatic
        @BeforeClass
        fun initClass() {
            server = StartServerForTesting()
        }

        @JvmStatic
        @AfterClass
        fun teardownClass() {
            server.close()
        }
    }

    @Test
    fun `ping endpoint works`() {
        val response = server.client(Request(GET, "http://localhost:${server.dependencies.config.port()}/ping"))

        assertThat(response.bodyString()).isEqualTo("pong")
    }
}