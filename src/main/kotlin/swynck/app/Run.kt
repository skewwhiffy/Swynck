package swynck.app

import org.http4k.server.Undertow
import org.http4k.server.asServer
import swynck.config.Config
import swynck.db.DataSourceFactory
import swynck.db.Migrations

fun main(args: Array<String>) = Run(Config())

object Run {
    operator fun invoke(config: Config) = this(config, DataSourceFactory(config))

    operator fun invoke(config: Config, dataSourceFactory: DataSourceFactory) {
        println("Applying migrations")
        Migrations(dataSourceFactory).run()
        println("Starting server on port ${config.port()}")
        App().asServer(Undertow(config.port())).start()
        println("Server started")
    }
}