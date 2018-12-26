package swynck.app

import org.http4k.server.Undertow
import org.http4k.server.asServer
import swynck.daemon.task.FileDetailsSync
import swynck.db.Migrations

fun main(args: Array<String>) = Run(Dependencies())

object Run {
    operator fun invoke(dependencies: Dependencies) {
        println("Applying migrations")
        Migrations(dependencies.dataSourceFactory).run()
        println("Starting server on port ${dependencies.config.port()}")
        App(dependencies).asServer(Undertow(dependencies.config.port())).start()
        dependencies
            .userRepository
            .getUser()
            ?.let { FileDetailsSync(it, dependencies) }
            ?.let { dependencies.daemonRunner.add(it) }
        println("Swynck started")
    }
}