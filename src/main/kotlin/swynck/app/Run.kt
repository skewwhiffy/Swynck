package swynck.app

import org.http4k.server.Undertow
import org.http4k.server.asServer

fun main(args: Array<String>) = println("Hello world")

object Run {
    operator fun invoke(port: Int) {
        App().asServer(Undertow(port)).start()
    }
}