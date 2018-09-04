package swynck.app

import org.http4k.server.Undertow
import org.http4k.server.asServer
import swynck.daemon.DaemonRunner
import swynck.daemon.task.FileDetailsSync
import swynck.db.Migrations

fun main(args: Array<String>) = Run(Dependencies())

object Run {
    operator fun invoke(dependencies: Dependencies) {
        println("Applying migrations")
        Migrations(dependencies.dataSourceFactory).run()
        println("Starting server on port ${dependencies.config.port()}")
        App(
            dependencies.userRepository,
            dependencies.oneDrive
        ).asServer(Undertow(dependencies.config.port())).start()
        println("Starting daemon runner")
        val daemon = DaemonRunner()
        dependencies.userRepository.getUser()?.let { FileDetailsSync(it) }?.let { daemon.add(it) }
        println("Swynck started")
    }
}