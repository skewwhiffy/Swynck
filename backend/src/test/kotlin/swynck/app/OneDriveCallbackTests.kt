package swynck.app

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status.Companion.ACCEPTED
import org.junit.Test
import swynck.app.api.Api
import swynck.common.defaultRedirectUri
import swynck.common.model.User
import swynck.real.onedrive.client.OnedriveWrapper
import swynck.real.onedrive.dto.AccessToken
import swynck.test.utils.TestDependencies
import swynck.test.utils.with
import java.net.URI
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
        val oneDrive = mockk<OnedriveWrapper>()
        val dependencies = TestDependencies().with(oneDrive)
        val api = Api(dependencies)
        val redirectUri = dependencies.config.defaultRedirectUri()
        every { oneDrive.getAccessToken(authCode, redirectUri) } returns accessToken
        every { oneDrive.getUser(accessToken, redirectUri) } returns
            User(id, displayName, redirectUri.toString(), accessToken.refresh_token)

        val response = """{"authCode":"$authCode"}"""
            .let { Request(POST, "/onedrive/authcode").body(it) }
            .let { api(it) }

        verify { oneDrive.getAccessToken(authCode, redirectUri) }
        val user = dependencies.userRepository.getUser() ?: throw AssertionError("Expected user entry")
        assertThat(user.refreshToken).isEqualTo(accessToken.refresh_token)
        assertThat(user.displayName).isEqualTo(displayName)
        assertThat(response.status).isEqualTo(ACCEPTED)
    }
}
