package swynck.db

import org.flywaydb.core.Flyway
import swynck.config.Config

class Migrations(private val config: Config) {
    fun run() {
        val flyway = Flyway()
        flyway.setDataSource(config.db(), "sa", "")
        flyway.migrate()
    }
}