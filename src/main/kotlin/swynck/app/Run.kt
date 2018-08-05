package swynck.app

import org.http4k.server.Undertow
import org.http4k.server.asServer
import swynck.db.Migrations

fun main(args: Array<String>) = Run(9000)

object Run {
    operator fun invoke(port: Int) {
        Migrations().run()
        App().asServer(Undertow(port)).start()
    }
}