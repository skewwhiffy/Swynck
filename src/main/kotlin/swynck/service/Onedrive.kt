package swynck.service

import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import swynck.config.Config
import swynck.config.Json.auto
import swynck.config.canAuthenticateOnedrive
import swynck.dto.onedrive.DeltaResponse
import swynck.model.User
import java.net.PortUnreachableException
import java.net.URI
import java.net.URLEncoder

class Onedrive(private val config: Config) {
    companion object {
        private const val clientId = "21133f26-e5d8-486b-8b27-0801db6496a9"
        private const val clientSecret = "gcyhkJZK73!$:zqHNBE243}"
        private val scopes = setOf("files.readwrite", "offline_access")
    }

    private val redirectUri = "http://localhost:${config.port()}"

    fun authenticationUrl(): URI {
        return if (!config.canAuthenticateOnedrive()) {
            throw PortUnreachableException("Authentication not supported on port ${config.port()}")
        } else mapOf(
            "client_id" to clientId,
            "scope" to scopes.joinToString(" "),
            "redirect_uri" to redirectUri,
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
            "redirect_uri" to redirectUri,
            "client_secret" to clientSecret,
            "grant_type" to "authorization_code",
            "code" to authCode
        )
            .mapValues { v -> v.value.let { URLEncoder.encode(it, "UTF-8") } }
            .map { "${it.key}=${it.value}" }
            .joinToString("&")
            .let { Request(POST, "https://login.live.com/oauth20_token.srf").body(it) }
            .header("Content-Type", "application/x-www-form-urlencoded")
        val client = OkHttp()
        val response = client(request)
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
            .let { Request(POST, "https://login.live.com/oauth20_token.srf").body(it) }
            .header("Content-Type", "application/x-www-form-urlencoded")
        val client = OkHttp()
        val response = client(request)
        return if (response.status.successful) AccessToken(response)
        else throw IllegalArgumentException("Problem getting access token: ${response.bodyString()}")
    }

    fun getUser(accessToken: AccessToken): User {
        val client = OkHttp()
        return Request(GET, "https://graph.microsoft.com/v1.0/me/drive")
            .header("Authorization", "bearer ${accessToken.access_token}")
            .let { client(it) }
            .let { DriveResource(it) }
            .let { it.owner.user }
            .let { User(it.id, it.displayName, redirectUri, accessToken.refresh_token) }
    }

    fun getDelta(accessToken: AccessToken, nextLink: URI? = null): DeltaResponse {
        val client = OkHttp()
        nextLink ?: return getDelta(
            accessToken,
            URI("https://graph.microsoft.com/v1.0/me/drive/root/delta")
        )
        return Request(GET, nextLink.toString())
            .header("Authorization", "bearer ${accessToken.access_token}")
            .let { client(it) }
            .also { println(it.bodyString()) }
            .let { DeltaResponse(it) }
    }
}

data class DriveResource(
    val id: String,
    val owner: IdentitySetResource
) {
    companion object {
        private val lens = Body.auto<DriveResource>().toLens()
        operator fun invoke(response: Response) = lens(response)
        data class IdentitySetResource(
            val user: IdentityResource
        ) {
            companion object {
                data class IdentityResource(
                    val displayName: String,
                    val id: String
                )
            }
        }
    }
}

data class AccessToken(
    val refresh_token: String,
    val access_token: String,
    val expires_in: Int
) {
    companion object {
        private val lens = Body.auto<AccessToken>().toLens()
        operator fun invoke(response: Response) = lens(response)
    }
}