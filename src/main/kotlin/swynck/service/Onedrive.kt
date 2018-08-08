package swynck.service

import swynck.config.Json.auto
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.nuxeo.onedrive.client.OneDriveAPI
import org.nuxeo.onedrive.client.OneDriveBasicAPI
import org.nuxeo.onedrive.client.OneDriveEmailAccount
import swynck.config.Config
import swynck.config.canAuthenticateOnedrive
import java.net.PortUnreachableException
import java.net.URI
import java.net.URLEncoder

open class Onedrive(private val config: Config) {
    companion object {
        private val clientId = "21133f26-e5d8-486b-8b27-0801db6496a9"
        private val clientSecret = "gcyhkJZK73!$:zqHNBE243}"
        private val callbackPorts = setOf(80, 8080, 9000, 38080)
        private val scopes = setOf("files.readwrite", "offline_access")
    }

    private val redirectUrl = "http://localhost:${config.port()}"

    fun authenticationUrl(): URI {
        return if (!config.canAuthenticateOnedrive()) {
            throw PortUnreachableException("Authentication only supported when running on the following ports: ${callbackPorts.joinToString()}")
        } else mapOf(
            "client_id" to clientId,
            "scope" to scopes.joinToString(" "),
            "redirect_url" to redirectUrl,
            "response_type" to "code"
        )
            .mapValues { v -> v.value.let { URLEncoder.encode(it, "UTF-8") } }
            .map { "${it.key}=${it.value}" }
            .joinToString("&")
            .let { URI("https://login.live.com/oauth20_authorize.srf?$it") }
    }

    open fun getAccessToken(authCode: String): AccessToken {
        val request = mapOf(
            "client_id" to clientId,
            "redirect_uri" to redirectUrl,
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
        return AccessToken(response)
    }

    open fun getEmail(accessToken: AccessToken): String {
        return OneDriveEmailAccount.getCurrentUserEmailAccount(accessToken.api())
    }

    private fun AccessToken.api(): OneDriveAPI = OneDriveBasicAPI(this.access_token)
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