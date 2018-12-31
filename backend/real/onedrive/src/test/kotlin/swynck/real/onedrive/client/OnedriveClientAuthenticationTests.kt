package swynck.real.onedrive.client

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import swynck.test.util.TestConfig
import java.net.PortUnreachableException

class OnedriveClientAuthenticationTests {
    private val config = TestConfig()
    private val onedrive = OnedriveClient(config)

    @Test
    fun `when using unsupported callback port then authentication URL blows up`() {
        config.port = 0

        assertThatThrownBy { onedrive.authenticationUrl() }.isInstanceOf(PortUnreachableException::class.java)
    }

    @Test
    fun `when using supported callback port then authentication URL is returned`() {
        config.port = 38080

        val url = onedrive.authenticationUrl()

        val queryVars = url.query!!
        println(queryVars)
    }
}
