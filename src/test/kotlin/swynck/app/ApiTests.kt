package swynck.app

import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.junit.Before
import org.junit.Test
import swynck.db.Migrations
import swynck.service.Onedrive
import swynck.test.utils.TestConfig
import java.util.*

class OnedriveCallbackTests {
    @Test
    fun `onedrive callback requests refresh token`() {
        val dependencies = Dependencies(TestConfig())
        val authToken = "${UUID.randomUUID()}"
        val onedrive = mockk<Onedrive>()
        val api = Api(dependencies.userRepository, onedrive)

        val response = api(Request(GET, "/onedrive?code=$authToken"))

        verify { onedrive.getRefreshToken(authToken) }
        // TODO: puts refresh token in DB
        // TODO: redirects to root
    }
}

class CurrentUserTests {
    private lateinit var config: TestConfig
    private lateinit var dependencies: Dependencies
    private lateinit var api: RoutingHttpHandler

    @Before
    fun init() {
        config = TestConfig()
        dependencies = Dependencies(config)
        Migrations(dependencies.dataSourceFactory).run()
        api = Api(dependencies.userRepository, dependencies.oneDrive)
    }
    @Test
    fun `current user endpoint returns redirect object when not logged in`() {
        config.port = 80
        val response = api(Request(GET, "/user/me"))

        assertThat(response.status).isEqualTo(OK)
        val notFoundResponse = UserNotFound.lens(response)
        assertThat(notFoundResponse.redirect.toString()).contains("live.com")
    }

    @Test
    fun `current user endpoint returns current user name`() {
        dependencies.dataSourceFactory.dataSource().connection!!.use {
            it.createStatement().execute("""
                insert into public.users (name, refreshToken) values ('the_name', 'the_token')
            """.trimIndent())
        }

        val response = api(Request(GET, "/user/me"))

        assertThat(response.status).isEqualTo(OK)
        val user = User.lens(response)
        assertThat(user.name).isEqualTo("the_name")
    }


}

