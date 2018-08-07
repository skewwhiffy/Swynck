package swynck.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConfigTests {
    @Test
    fun `config allows authentication when port is configured`() {
        setOf(
            80,
            8080,
            9000,
            38080
        )
            .forEach {
                val config = object: Config {
                    override fun port() = it
                    override fun db() = TODO()
                }

                assertThat(config.canAuthenticateOnedrive()).isTrue()
            }
    }

    @Test
    fun `config does not allow authentication to Onedrive when port is not configured`() {
        val config = object: Config {
            override fun port() = 0
            override fun db() = TODO()
        }

        assertThat(config.canAuthenticateOnedrive()).isFalse()
    }
}