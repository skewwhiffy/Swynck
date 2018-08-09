package swynck.app

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.MOVED_PERMANENTLY
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.junit.Before
import org.junit.Test
import swynck.db.Migrations
import swynck.service.AccessToken
import swynck.service.Onedrive
import swynck.test.utils.TestConfig
import java.util.*

class OnedriveCallbackTests {
    @Test
    fun `onedrive callback populates refresh token`() {
        val dependencies = Dependencies(TestConfig())
        Migrations(dependencies.dataSourceFactory).run()
        val authCode = "${UUID.randomUUID()}"
        val accessToken = AccessToken(
            "${UUID.randomUUID()}",
            "${UUID.randomUUID()}",
            5
        )
        val email = "${UUID.randomUUID()}@test.com"
        val onedrive = mockk<Onedrive>()
        val api = Api(dependencies.userRepository, onedrive)
        every { onedrive.getAccessToken(authCode) } returns accessToken
        every { onedrive.getEmail(accessToken) } returns email

        val response = api(Request(GET, "/onedrive?code=$authCode"))

        verify { onedrive.getAccessToken(authCode) }
        val user = dependencies.userRepository.getUser() ?: throw AssertionError("Expected user entry")
        assertThat(user.refreshToken).isEqualTo(accessToken.refresh_token)
        assertThat(user.email).isEqualTo(email)
        assertThat(response.status).isEqualTo(MOVED_PERMANENTLY)
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
        config.port = 38080
        val response = api(Request(GET, "/user/me"))

        assertThat(response.status).isEqualTo(OK)
        val notFoundResponse = UserNotFound.lens(response)
        assertThat(notFoundResponse.redirect.toString()).contains("live.com")
    }

    @Test
    fun `current user endpoint returns current user name`() {
        dependencies.dataSourceFactory.dataSource().connection!!.use {
            it.createStatement().execute("""
                insert into users (email, refreshToken) values ('the_email', 'the_token')
            """.trimIndent())
        }

        val response = api(Request(GET, "/user/me"))

        assertThat(response.status).isEqualTo(OK)
        val user = User.lens(response)
        assertThat(user.email).isEqualTo("the_email")
    }
}

