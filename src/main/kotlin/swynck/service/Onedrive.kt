package swynck.service

import swynck.config.Config
import swynck.config.canAuthenticateOnedrive
import java.net.PortUnreachableException
import java.net.URI
import java.net.URLEncoder

class Onedrive(private val config: Config) {
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
}