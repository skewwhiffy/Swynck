package swynck.app

import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.junit.Test
import swynck.test.utils.TestDependencies

class AppTests {
    private val dependencies = TestDependencies()
    private val app = App(dependencies)

    @Test
    fun `app ping endpoint responds`() {
        val response = app(Request(GET, "/ping"))

        assertThat(response).matches { it.status.successful }
        assertThat(response.bodyString()).isEqualTo("pong")
    }

    @Test
    fun `api is correctly wired in`() {
        val dependencies = TestDependencies()
        val app = App(dependencies)

        val response = app(Request(GET, "/api/ping"))

        assertThat(response).satisfies { it.status.successful }
        assertThat(response.bodyString()).isEqualTo("pong")
    }
}