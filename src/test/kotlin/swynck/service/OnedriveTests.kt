package swynck.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import swynck.config.Config
import java.net.PortUnreachableException

class OnedriveTests {
    @Test
    fun `when using unsupported callback port then authentication URL blows up`() {
        val config = object : Config {
            override fun port() = 3
            override fun db() = TODO()
        }
        val onedrive = Onedrive(config)

        assertThatThrownBy { onedrive.authenticationUrl() }.isInstanceOf(PortUnreachableException::class.java)
    }
}