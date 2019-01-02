package swynck.fake.onedrive

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.routing.bind
import org.http4k.routing.routes
import swynck.common.extensions.queryMap
import swynck.fake.onedrive.testdata.FakeOnedriveTestData
import swynck.real.onedrive.client.OnedriveClients
import swynck.real.onedrive.dto.AccessToken
import swynck.real.onedrive.dto.DeltaResponse
import swynck.real.onedrive.dto.DriveResource
import swynck.real.onedrive.dto.DriveResource.Companion.IdentitySetResource
import swynck.real.onedrive.dto.DriveResource.Companion.IdentitySetResource.Companion.IdentityResource
import java.net.URI
import java.util.*

class FakeOnedriveClients : OnedriveClients {
    private val fakeOnedriveTestData = FakeOnedriveTestData()
    private var deltaLinkRequested = false
    var currentAccessToken = generateRandomAccessToken()
    val authCode = "${UUID.randomUUID()}"
    val hasDeltaLinkBeenRequested get() = deltaLinkRequested

    override val authClient = { it: Request -> handleAuth(it) }
    override val graphClient = routes(
        "v1.0/me/drive" bind GET to { getUser(it) },
        "v1.0/me/drive/root/delta" bind GET to { getDelta(it) }
    )

    private fun handleAuth(request: Request): Response {
        val bodyMap = request.bodyString().queryMap()
        if (bodyMap["code"] != authCode && bodyMap["refresh_token"] != currentAccessToken.refresh_token) return Response(UNAUTHORIZED)
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

    private fun getDelta(request: Request): Response {
        fakeOnedriveTestData.getDelta(URI(request.uri.toString()))?.let { return Response(OK).withBody(it) }
        val deltaLink = fakeOnedriveTestData.getDeltaLink() ?: return Response(NOT_FOUND)
        if (deltaLink.queryMap()["token"] != request.uri.query.queryMap()["token"]) return Response(NOT_FOUND)
        deltaLinkRequested = true
        return DeltaResponse(
            null,
            deltaLink,
            listOf()
        )
            .let { Response(OK).withBody(it) }
    }

    private fun getUser(request: Request): Response {
        if (request.header("Authorization") != "bearer ${currentAccessToken.access_token}") return Response(UNAUTHORIZED)
        val user = fakeOnedriveTestData.user ?: return Response(INTERNAL_SERVER_ERROR).body("No test user found")
        val id = IdentityResource(
            user.displayName,
            user.id
        )
        val setResource = IdentitySetResource(id)
        val resource = DriveResource(user.id, setResource)
        return Response(OK).withBody(resource)
    }

    private fun Response.withBody(resource: DriveResource) = DriveResource
        .lens
        .inject(resource, this)

    private fun Response.withBody(delta: DeltaResponse) = DeltaResponse
        .lens
        .inject(delta, this)
}
