package swynck.config

import com.statemachinesystems.envy.Default
import com.statemachinesystems.envy.Envy

interface Config {
    companion object {
        operator fun invoke() = Envy.configure(Config::class.java) as Config
    }

    @Default("38080")
    fun port(): Int

    @Default("jdbc:h2:~/.config/swynck/swynck")
    fun db(): String
}

private val callbackPorts = setOf(
    8080,
    9000,
    38080
)

fun Config.canAuthenticateOnedrive() = callbackPorts.contains(port())