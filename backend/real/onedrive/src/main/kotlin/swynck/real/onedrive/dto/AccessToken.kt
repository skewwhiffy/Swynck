package swynck.real.onedrive.dto

import org.http4k.core.Body
import org.http4k.core.Response
import swynck.common.Json.auto

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
