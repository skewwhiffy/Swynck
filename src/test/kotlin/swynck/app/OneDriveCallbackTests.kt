package swynck.app

import assertk.assert
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.ACCEPTED
import org.junit.Test
import swynck.app.api.Api
import swynck.model.User
import swynck.service.AccessToken
import swynck.service.Onedrive
import swynck.test.utils.TestDependencies
import swynck.test.utils.with
import java.util.*

class OneDriveCallbackTests {
    @Test
    fun `one drive callback populates refresh token`() {
        val authCode = "${UUID.randomUUID()}"
        val accessToken = AccessToken(
            "${UUID.randomUUID()}",
            "${UUID.randomUUID()}",
            5
        )
        val id = "${UUID.randomUUID()}"
        val displayName = "${UUID.randomUUID()}"
        val oneDrive = mockk<Onedrive>()
        val dependencies = TestDependencies().with(oneDrive)
        val api = Api(dependencies)
        val redirectUri = "${UUID.randomUUID()}.com"
        every { oneDrive.getAccessToken(authCode) } returns accessToken
        every { oneDrive.getUser(accessToken) } returns
            User(id, displayName, redirectUri, accessToken.refresh_token)

        val response = """
            {"authCode":"$authCode"}
        """.trimIndent()
            .let { Request(POST, "/onedrive/authcode").body(it) }
            .let { api(it) }

        verify { oneDrive.getAccessToken(authCode) }
        val user = dependencies.userRepository.getUser() ?: throw AssertionError("Expected user entry")
        assert(user.refreshToken).isEqualTo(accessToken.refresh_token)
        assert(user.displayName).isEqualTo(displayName)
        assert(response.status).isEqualTo(ACCEPTED)
    }
}
