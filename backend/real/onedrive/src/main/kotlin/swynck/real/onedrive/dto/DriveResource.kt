package swynck.real.onedrive.dto

import org.http4k.core.Body
import org.http4k.core.Response
import swynck.common.Json.auto

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

