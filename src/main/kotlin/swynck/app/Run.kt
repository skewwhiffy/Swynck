package swynck.app

import org.http4k.server.Undertow
import org.http4k.server.asServer
import swynck.config.Config
import swynck.db.Migrations

fun main(args: Array<String>) = Run(Config())

object Run {
    operator fun invoke(config: Config) {
        Migrations(config).run()
        App().asServer(Undertow(config.port())).start()
    }
}