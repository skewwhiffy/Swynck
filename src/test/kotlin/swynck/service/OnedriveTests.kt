package swynck.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import swynck.test.utils.TestConfig
import java.net.PortUnreachableException

class OnedriveTests {
    private lateinit var config: TestConfig
    private lateinit var onedrive: Onedrive

    @Before
    fun init() {
        config = TestConfig()
        onedrive = Onedrive(config)
    }

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