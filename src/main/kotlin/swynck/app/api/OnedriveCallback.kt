package swynck.app.api

import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.ACCEPTED
import swynck.config.Json.auto
import swynck.db.UserRepository
import swynck.service.Onedrive

object OnedriveCallback {
    operator fun invoke(
        onedrive: Onedrive,
        userRepository: UserRepository,
        request: Request
    ): Response {
        val requestDeserialized = OnedriveCallbackRequest(request)
        val accessToken = onedrive.getAccessToken(requestDeserialized.authCode)
        val userDetails = onedrive.getUser(accessToken)
        userRepository.addUser(userDetails)
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
