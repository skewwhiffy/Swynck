package swynck.db

import org.flywaydb.core.Flyway

class Migrations {
    fun run() {
        val flyway = Flyway()
        flyway.setDataSource("jdbc:h2:~/.config/swynck", "sa", "")
        flyway.migrate()
    }
}