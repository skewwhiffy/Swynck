package swynck.real.onedrive.client

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import swynck.common.Config
import swynck.common.Json
import swynck.common.canAuthenticateOnedrive
import swynck.common.model.User
import swynck.real.onedrive.dto.AccessToken
import swynck.real.onedrive.dto.DeltaResponse
import swynck.real.onedrive.dto.DriveResource
import java.net.PortUnreachableException
import java.net.URI
import java.net.URLEncoder

class OnedriveWrapper(
    private val onedriveClients: OnedriveClients,
    private val config: Config
) {
    @Suppress("SpellCheckingInspection")
    companion object {
        const val clientId = "21133f26-e5d8-486b-8b27-0801db6496a9"
        const val clientSecret = "gcyhkJZK73!$:zqHNBE243}"
        val scopes = setOf("files.readwrite", "offline_access")
    }

    private val redirectUri = URI("http://localhost:${config.port()}")

    fun authenticationUrl(redirectUri: URI = this.redirectUri): URI {
        return if (!config.canAuthenticateOnedrive()) {
            throw PortUnreachableException("Authentication not supported on port ${config.port()}")
        } else mapOf(
                "client_id" to clientId,
                "scope" to scopes.joinToString(" "),
                "redirect_uri" to redirectUri.toString(),
                "response_type" to "code"
        )
                .mapValues { v -> v.value.let { URLEncoder.encode(it, "UTF-8") } }
                .map { "${it.key}=${it.value}" }
                .joinToString("&")
                .let { URI("https://login.live.com/oauth20_authorize.srf?$it") }
    }

    fun getAccessToken(authCode: String): AccessToken {
        val request = mapOf(
                "client_id" to clientId,
                "redirect_uri" to redirectUri.toString(),
                "client_secret" to clientSecret,
                "grant_type" to "authorization_code",
                "code" to authCode
        )
                .mapValues { v -> v.value.let { URLEncoder.encode(it, "UTF-8") } }
                .map { "${it.key}=${it.value}" }
                .joinToString("&")
                .let { Request(Method.POST, "oauth20_token.srf").body(it) }
                .header("Content-Type", "application/x-www-form-urlencoded")
        val response = onedriveClients.authClient(request)
        return if (response.status.successful) AccessToken(response)
        else throw IllegalArgumentException("Problem getting access token: ${response.bodyString()}")
    }

    fun getAccessToken(user: User): AccessToken {
        val request = mapOf(
                "client_id" to clientId,
                "redirect_uri" to user.redirectUri,
                "client_secret" to clientSecret,
                "grant_type" to "refresh_token",
                "refresh_token" to user.refreshToken
        )
                .mapValues { v -> v.value.let { URLEncoder.encode(it, "UTF-8") } }
                .map { "${it.key}=${it.value}" }
                .joinToString("&")
                .let { Request(Method.POST, "oauth20_token.srf").body(it) }
                .header("Content-Type", "application/x-www-form-urlencoded")
        val response = onedriveClients.authClient(request)
        return if (response.status.successful) AccessToken(response)
        else throw IllegalArgumentException("Problem getting access token: ${response.bodyString()}")
    }

    fun getUser(accessToken: AccessToken): User {
        return Request(Method.GET, "v1.0/me/drive")
                .header("Authorization", "bearer ${accessToken.access_token}")
                .let { onedriveClients.graphClient(it) }
                .let { DriveResource(it) }
                .let { it.owner.user }
                .let { User(it.id, it.displayName, redirectUri.toString(), accessToken.refresh_token) }
    }

    fun getDelta(accessToken: AccessToken, nextLink: URI? = null) = getDeltaRaw(accessToken, nextLink)
        .bodyString()
        .let { Json.asA(it, DeltaResponse::class) }

    fun getDeltaRaw(accessToken: AccessToken, nextLink: URI? = null): Response {
        nextLink ?: return getDeltaRaw(
            accessToken,
            URI("v1.0/me/drive/root/delta")
        )
        return Request(Method.GET, nextLink.path)
            .header("Authorization", "bearer ${accessToken.access_token}")
            .let { onedriveClients.graphClient(it) }
    }
}
