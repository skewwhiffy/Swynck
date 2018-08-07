package swynck.app

import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.junit.Test
import swynck.test.utils.TestConfig

class AppTests {
    @Test
    fun `api ping endpoint responds`() {
        val dependencies = Dependencies(TestConfig())
        val app = App(dependencies.userRepository, dependencies.oneDrive)

        val response = app(Request(GET, "/api/ping"))

        assertThat(response.status.successful).isTrue()
        assertThat(response.bodyString()).isEqualTo("pong")
    }
}