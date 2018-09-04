package swynck.dto.onedrive

import com.fasterxml.jackson.annotation.JsonProperty
import org.http4k.core.Body
import org.http4k.core.Response
import swynck.config.Json.auto
import java.net.URI

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

data class DriveItem(
    val id: String,
    val name: String,
    val file: FileItem?,
    val folder: FolderItem?,
    val parentReference: ParentReference
)

data class FileItem(
    val mimeType: String
)

data class FolderItem(
    val childCount: Int
)

data class ParentReference(
    val driveId: String,
    val id: String
)