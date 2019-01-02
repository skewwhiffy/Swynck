package swynck.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import swynck.test.utils.TestDependencies

class OnedriveAccessTokenTests {
    private val dependencies = TestDependencies()

    @Test
    fun `can get refresh token`() {
        dependencies.addValidUser()
        val user = dependencies.userRepository.getUser()!!
        val accessToken = dependencies.oneDrive.getAccessToken(user)

        assertThat(accessToken.access_token).isNotBlank()
    }
}