package swynck.fake.onedrive

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import swynck.common.Json.auto
import swynck.real.onedrive.client.OnedriveClients
import swynck.real.onedrive.dto.AccessToken
import java.util.*

class FakeOnedriveClients : OnedriveClients {
    var currentAccessToken = generateRandomAccessToken()
    val authCode = "${UUID.randomUUID()}"

    override val authClient = { it: Request -> handleAuth(it) }
    override val graphClient: HttpHandler
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    private fun handleAuth(request: Request): Response {
        val bodyMap = request.bodyString().split("&").map { it.split("=") }.map { it[0] to it[1] }.toMap()
        if (bodyMap["code"] != authCode) return Response(UNAUTHORIZED)
        return Response(OK).withBody(currentAccessToken)
    }

    private fun generateRandomAccessToken() = AccessToken(
        "${UUID.randomUUID()}",
        "${UUID.randomUUID()}",
        500
    )

    private fun Response.withBody(accessToken: AccessToken) = AccessToken
        .lens
        .inject(accessToken, this)
}
