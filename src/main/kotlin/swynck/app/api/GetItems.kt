package swynck.app.api

import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import swynck.config.Json.auto
import swynck.db.File
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
        val files = metadataRepository.getFiles(currentUser, rootFolder)
        val response = GetItemsResponse(
            files.toDto()
        )
        return Response(OK).withBody(response)
    }
}

data class GetItemsResponse(
    val files: List<FileDto>
) {
    companion object {
        val lens = Body.auto<GetItemsResponse>().toLens()
    }
}

data class FileDto(
    val name: String,
    val mime: String
)

private fun Response.withBody(response: GetItemsResponse) = GetItemsResponse
    .lens
    .inject(response, this)

private fun Collection<File>.toDto() = map { it.toDto() }.toList()

private fun File.toDto() = FileDto(name, mimeType)