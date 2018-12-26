package swynck.app.api

import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import swynck.config.Json.auto
import swynck.db.File
import swynck.db.Folder
import swynck.db.OnedriveMetadataRepository
import swynck.db.UserRepository

object GetItems {
    operator fun invoke(
        userRepository: UserRepository,
        metadataRepository: OnedriveMetadataRepository,
        request: Request
    ): Response {
        val currentUser = userRepository.getUser() ?: return Response(FORBIDDEN)
        val rootFolder = metadataRepository.getRootFolder(currentUser)
        val folders = metadataRepository.getFolders(currentUser, rootFolder)
        val files = metadataRepository.getFiles(currentUser, rootFolder)
        val response = GetItemsResponse(
            folders.map { it.toDto() },
            files.map { it.toDto() }
        )
        return Response(OK).withBody(response)
    }
}

data class GetItemsResponse(
    val folders: List<FolderDto>,
    val files: List<FileDto>
) {
    companion object {
        val lens = Body.auto<GetItemsResponse>().toLens()
    }
}

data class FolderDto(
    val name: String
)

data class FileDto(
    val name: String,
    val mime: String
)

private fun Response.withBody(response: GetItemsResponse) = GetItemsResponse
    .lens
    .inject(response, this)

private fun Folder.toDto() = FolderDto(name)
private fun File.toDto() = FileDto(name, mimeType)