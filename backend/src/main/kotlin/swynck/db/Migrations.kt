package swynck.db

import org.flywaydb.core.Flyway
import swynck.app.Dependencies

class Migrations(private val dependencies: Dependencies) {
    fun run() {
        val flyway = Flyway()
        flyway.dataSource = dependencies.dataSourceFactory.dataSource()
        flyway.migrate()
    }
}