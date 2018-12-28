package swynck.app.api

import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import swynck.config.Json.auto
import swynck.db.File
import swynck.db.Folder
import swynck.db.OnedriveMetadataRepository
import swynck.db.UserRepository

class ItemsRoutes(
    private val userRepository: UserRepository,
    private val metadataRepository: OnedriveMetadataRepository
) : RoutingHttpHandler by routes(
    "/ping" bind GET to { Response(OK).body("pong") },
    "/" bind GET to { GetItems(userRepository, metadataRepository, it) }
)

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