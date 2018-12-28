package swynck.config

import assertk.assert
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.Test

class ConfigTests {
    @Test
    fun `config allows authentication when port is configured`() {
        val config = object : Config {
            override fun port() = 38080
            override fun db() = TODO()
        }

        assert(config.canAuthenticateOnedrive()).isTrue()
    }

    @Test
    fun `config does not allow authentication to Onedrive when port is not configured`() {
        val config = object: Config {
            override fun port() = 0
            override fun db() = TODO()
        }

        assert(config.canAuthenticateOnedrive()).isFalse()
    }
}