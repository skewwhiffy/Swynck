package swynck.app

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.ACCEPTED
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.junit.Before
import org.junit.Test
import swynck.db.Migrations
import swynck.model.User
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
        val id = "${UUID.randomUUID()}"
        val displayName = "${UUID.randomUUID()}"
        val onedrive = mockk<Onedrive>()
        val api = Api(dependencies.userRepository, onedrive)
        val redirectUri = "${UUID.randomUUID()}.com"
        every { onedrive.getAccessToken(authCode) } returns accessToken
        every { onedrive.getUser(accessToken) } returns
            User(id, displayName, redirectUri, accessToken.refresh_token)

        val response = """
            {"authCode":"$authCode"}
        """.trimIndent()
            .let { Request(POST, "/onedrive/authcode").body(it) }
            .let { api(it) }

        verify { onedrive.getAccessToken(authCode) }
        val user = dependencies.userRepository.getUser() ?: throw AssertionError("Expected user entry")
        assertThat(user.refreshToken).isEqualTo(accessToken.refresh_token)
        assertThat(user.displayName).isEqualTo(displayName)
        assertThat(response.status).isEqualTo(ACCEPTED)
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
                insert into users (id, displayName, refreshToken)
                values ('the_id', 'the_display_name', 'the_token')
            """.trimIndent())
        }

        val response = api(Request(GET, "/user/me"))

        assertThat(response.status).isEqualTo(OK)
        val user = UserFound.lens(response)
        assertThat(user.displayName).isEqualTo("the_display_name")
    }
}

