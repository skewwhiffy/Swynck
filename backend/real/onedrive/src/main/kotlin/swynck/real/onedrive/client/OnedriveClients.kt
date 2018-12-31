package swynck.real.onedrive.client

import org.http4k.client.OkHttp
import org.http4k.core.HttpHandler
import org.http4k.core.Request

interface OnedriveClients {
    val authClient: HttpHandler
    val graphClient: HttpHandler
}

class OnedriveClientsImpl : OnedriveClients {
    private val ok = OkHttp()

    override val authClient = { it: Request -> ok(it) }
    override val graphClient = { it: Request -> ok(it)}
}