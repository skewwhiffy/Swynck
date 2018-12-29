package swynck.test.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.http4k.client.OkHttp
import org.http4k.core.Method.GET
import org.http4k.core.Request
import swynck.app.Run
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class StartServerForTesting : AutoCloseable {
    private val scope = CoroutineScope(EmptyCoroutineContext)
    val dependencies = TestDependencies()

    private val server = scope.async {
        try {
            Run(dependencies)
        } catch (e: Exception) {
            println(e)
            throw e
        }
    }
    val client = OkHttp()

    init {
        var attempts = 10
        while(true) {
            val response = client(Request(GET, "http://localhost:${dependencies.config.port()}/ping"))
            if (response.status.successful) break
            Thread.sleep(100)
            attempts--
            if (attempts < 0) throw IllegalStateException("Server did not come up")
        }
    }

    override fun close() {
        runBlocking { server.cancelAndJoin() }
    }
}