package swynck.db

import org.flywaydb.core.Flyway

class Migrations(private val dataSourceFactory: DataSourceFactory) {
    fun run() {
        val flyway = Flyway()
        flyway.dataSource = dataSourceFactory.dataSource()
        flyway.migrate()
    }
}