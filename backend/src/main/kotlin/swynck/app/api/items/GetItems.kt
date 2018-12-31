package swynck.app.api.items

import org.http4k.core.Body
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import swynck.app.Dependencies
import swynck.app.api.dto.FileDto
import swynck.app.api.dto.FolderDto
import swynck.app.api.dto.toDto
import swynck.common.Json.auto

object GetItems {
    operator fun invoke(dependencies: Dependencies): Response {
        val currentUser = dependencies.userRepository.getUser() ?: return Response(FORBIDDEN)
        val rootFolder = dependencies.metadata.getRootFolder(currentUser)
        val folders = dependencies.metadata.getFolders(currentUser, rootFolder)
        val files = dependencies.metadata.getFiles(currentUser, rootFolder)
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

private fun Response.withBody(response: GetItemsResponse) = GetItemsResponse
    .lens
    .inject(response, this)

