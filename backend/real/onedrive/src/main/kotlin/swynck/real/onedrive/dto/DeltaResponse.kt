package swynck.real.onedrive.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Body
import org.http4k.core.Response
import java.net.URI
import swynck.common.Json.auto

data class DeltaResponse(
        @JsonProperty("@odata.nextLink")
        val nextLink: URI?,
        @JsonProperty("@odata.deltaLink")
        val deltaLink: URI?,
        val value: List<DriveItem>
) {
    companion object {
        private val lens = Body.auto<DeltaResponse>().toLens()
        operator fun invoke(response: Response) = lens(response)
    }
}
