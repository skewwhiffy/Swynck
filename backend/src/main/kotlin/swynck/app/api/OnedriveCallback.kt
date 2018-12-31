package swynck.app.api

import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.ACCEPTED
import swynck.app.Dependencies
import swynck.common.Json.auto

object OnedriveCallback {
    operator fun invoke(
        dependencies: Dependencies,
        request: Request
    ): Response {
        val requestDeserialized = OnedriveCallbackRequest(request)
        val accessToken = dependencies.oneDrive.getAccessToken(requestDeserialized.authCode)
        val userDetails = dependencies.oneDrive.getUser(accessToken)
        dependencies.userRepository.addUser(userDetails)
        return Response(ACCEPTED)
    }
}

data class OnedriveCallbackRequest(
    val authCode: String
) {
    companion object {
        private val lens = Body.auto<OnedriveCallbackRequest>().toLens()
        operator fun invoke(request: Request) = lens(request)
    }
}
