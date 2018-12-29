package swynck.db

import org.flywaydb.core.Flyway
import swynck.app.Dependencies

class Migrations(private val dependencies: Dependencies) {
    fun run() = Flyway
        .configure()
        .dataSource(dependencies.dataSourceFactory.dataSource())
        .load()
        .migrate()
}