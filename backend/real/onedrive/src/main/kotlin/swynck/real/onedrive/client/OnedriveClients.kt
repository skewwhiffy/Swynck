package swynck.real.onedrive.client

import org.http4k.client.OkHttp
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Uri
import java.net.URL

interface OnedriveClients {
    val authClient: HttpHandler
    val graphClient: HttpHandler
}

class OnedriveClientsImpl : OnedriveClients {
    private val ok = OkHttp()

    override val authClient = proxyClient("https://login.live.com")

    override val graphClient = proxyClient("https://graph.microsoft.com")

    private fun proxyClient(baseUrl: String) = { it: Request -> ok(it.withBaseUrl(baseUrl)) }

    private fun Request.withBaseUrl(baseUrl: String) = URL(baseUrl)
        .let { URL(it, uri.toString()) }
        .toString()
        .let { Uri.of(it) }
        .let { uri(it) }
}