package swynck.config

import com.statemachinesystems.envy.Default
import com.statemachinesystems.envy.Envy
import com.statemachinesystems.envy.Name

interface Config {
    companion object {
        operator fun invoke() = Envy.configure(Config::class.java) as Config
    }

    @Default("9000")
    @Name("PORT")
    fun port(): Int

    @Default("jdbc:h2:~/.config/swynck")
    @Name("DB")
    fun db(): String
}