package swynck.service

import swynck.config.Config
import java.net.PortUnreachableException
import java.net.URI

class Onedrive(private val config: Config) {
    companion object {
        private val clientId = "21133f26-e5d8-486b-8b27-0801db6496a9"
        private val clientSecret = "gcyhkJZK73!$:zqHNBE243}"
        private val callbackPorts = setOf(80, 8080, 9000, 38080)
        private val scopes = setOf("files.readwrite", "offline_access")
    }

    fun authenticationUrl(): URI {
        if (!callbackPorts.contains(config.port())) {
            throw PortUnreachableException("Authentication only supported when running on the following ports: ${callbackPorts.joinToString()}")
        }
        TODO()
    }
}