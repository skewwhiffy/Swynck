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

object GetMusic {
    operator fun invoke(
        request: Request
    ): Response {
        return Response(OK).body("[]")
    }
}