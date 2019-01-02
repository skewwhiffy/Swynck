package swynck.real.onedrive.client

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import swynck.common.defaultRedirectUri
import swynck.test.util.TestConfig
import java.net.PortUnreachableException

class OnedriveClientAuthenticationTests {
    private val config = TestConfig()
    private val onedrive = OnedriveWrapper(OnedriveClientsImpl(), config)

    @Test
    fun `when using unsupported callback port then authentication URL blows up`() {
        config.port = 0

        assertThatThrownBy { onedrive.authenticationUrl(config.defaultRedirectUri()) }.isInstanceOf(PortUnreachableException::class.java)
    }

    @Test
    fun `when using supported callback port then authentication URL is returned`() {
        config.port = 38080

        val url = onedrive.authenticationUrl(config.defaultRedirectUri())

        val queryVars = url.query!!
        println(queryVars)
    }
}
