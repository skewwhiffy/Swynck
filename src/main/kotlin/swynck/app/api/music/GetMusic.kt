package swynck.app.api.music

import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import swynck.app.Dependencies
import swynck.app.api.dto.FileDto
import swynck.app.api.dto.toDto
import swynck.config.Json.auto

object GetMusic {
    private val musicMimeTypes = listOf(
        "audio/mpeg"
    )

    operator fun invoke(
        dependencies: Dependencies,
        request: Request
    ): Response {
        val currentUser = dependencies.userRepository.getUser() ?: return Response(FORBIDDEN)
        val files = dependencies
            .metadata
            .search(currentUser, *musicMimeTypes.toTypedArray())
            .map { it.toDto() }
        return Response(OK)
            .withBody(GetMusicResponse(files))
    }
}

data class GetMusicResponse(
    val files: List<FileDto>
) {
    companion object {
        val lens = Body.auto<GetMusicResponse>().toLens()
    }
}

private fun Response.withBody(response: GetMusicResponse) = GetMusicResponse
    .lens
    .inject(response, this)