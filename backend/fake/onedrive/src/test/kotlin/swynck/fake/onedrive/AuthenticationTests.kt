package swynck.fake.onedrive

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import swynck.common.defaultRedirectUri
import swynck.real.onedrive.client.OnedriveWrapper
import swynck.test.util.TestConfig

class AuthenticationTests {
    private val fakeClients = FakeOnedriveClients()
    val wrapper = OnedriveWrapper(fakeClients, TestConfig())

    @Test
    fun `can get access token from auth code`() {
        val token = wrapper.getAccessToken(fakeClients.authCode, TestConfig().defaultRedirectUri())

        assertThat(token.access_token).isNotNull()
    }

    @Test
    fun `when getting access token with wrong auth code then throws`() {
        assertThatThrownBy { wrapper.getAccessToken("Not_correct_code", TestConfig().defaultRedirectUri()) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}