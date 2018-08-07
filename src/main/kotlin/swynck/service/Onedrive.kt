package swynck.service

import swynck.config.Config
import swynck.config.canAuthenticateOnedrive
import java.net.PortUnreachableException
import java.net.URI
import java.net.URLEncoder
import java.time.ZonedDateTime

open class Onedrive(private val config: Config) {
    companion object {
        private val clientId = "21133f26-e5d8-486b-8b27-0801db6496a9"
        private val clientSecret = "gcyhkJZK73!$:zqHNBE243}"
        private val callbackPorts = setOf(80, 8080, 9000, 38080)
        private val scopes = setOf("files.readwrite", "offline_access")
    }

    fun authenticationUrl(): URI {
        return if (!config.canAuthenticateOnedrive()) {
            throw PortUnreachableException("Authentication only supported when running on the following ports: ${callbackPorts.joinToString()}")
        } else mapOf(
            "client_id" to clientId,
            "scope" to scopes.joinToString(" "),
            "redirect_url" to "http://localhost:${config.port()}/api/onedrive",
            "response_type" to "code"
        )
            .mapValues { v -> v.value.let { URLEncoder.encode(it, "UTF-8") } }
            .map { "${it.key}=${it.value}" }
            .joinToString("&")
            .let { URI("https://login.live.com/oauth20_authorize.srf?$it") }
    }

    open fun getAccessToken(authCode: String): AccessToken {
        TODO()
        /*
        var queryVariables = new Dictionary<string, string>
        {
            {"client_id", ClientId},
            {"redirect_uri", Callback},
            {"client_secret", ClientSecret}
        };
        var authToken = await GetAuthorizationCodeAsync(ct);
        queryVariables["grant_type"] = "authorization_code";
        queryVariables["code"] = authToken;
        var content = new FormUrlEncodedContent(queryVariables);
        var response = await client.PostAsync("https://login.live.com/oauth20_token.srf", content, ct);
        var payload = await response.Content.ReadAsStringAsync();
        token = RefreshTokenDetails.FromTokenResponse(payload);
        */
    }
}

data class AccessToken(
    val refresh_token: String,
    val access_token: String,
    val expires_in: Int
)