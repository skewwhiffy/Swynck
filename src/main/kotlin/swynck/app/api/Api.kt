package swynck.app.api

import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import swynck.app.Dependencies
import swynck.db.OnedriveMetadataRepository
import swynck.db.UserRepository
import swynck.service.Onedrive

class Api(
    userRepository: UserRepository,
    metadataRepository: OnedriveMetadataRepository,
    onedrive: Onedrive
): RoutingHttpHandler by routes(
    "/ping" bind GET to { Response(OK).body("pong") },
    "/user/me" bind GET to { GetCurrentUser(userRepository, onedrive) },
    "/onedrive/authcode" bind POST to { OnedriveCallback(onedrive, userRepository, it) },
    "items" bind ItemsRoutes(userRepository, metadataRepository),
    //"/items" bind GET to { ItemsRoutes(userRepository, metadataRepository, it) },
    "/music" bind GET to { GetMusic(it) }
) {
    constructor(dependencies: Dependencies): this(
        dependencies.userRepository,
        dependencies.metadata,
        dependencies.oneDrive
    )
}
