package swynck.app

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlinx.coroutines.experimental.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.http4k.client.OkHttp
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import swynck.config.Config
import swynck.test.utils.TestConfig

class RunTests {
    companion object {
        private lateinit var server: Deferred<*>
        private lateinit var client: OkHttp
        private lateinit var config: Config

        @JvmStatic
        @BeforeClass
        fun initClass() {
            config = TestConfig()
            server = async {
                try {
                    Run(config)
                } catch (e: Exception) {
                    println(e)
                    throw e
                }
            }
            client = OkHttp()
            var attempts = 10
            while(true) {
                val response = client(Request(GET, "http://localhost:${config.port()}/ping"))
                if (response.status.successful) return
                Thread.sleep(100)
                attempts--
                if (attempts < 0) throw IllegalStateException("Server did not come up")
            }
        }

        @JvmStatic
        @AfterClass
        fun teardownClass() {
            runBlocking { server.cancelAndJoin() }
        }
    }

    @Test
    fun `dummy test`() {
        val response = client(Request(GET, "http://localhost:${config.port()}/ping"))

        assertThat(response.bodyString()).isEqualTo("pong")
    }
}