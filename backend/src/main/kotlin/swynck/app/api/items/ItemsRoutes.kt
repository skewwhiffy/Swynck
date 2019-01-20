package swynck.app.api.items

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion
import org.http4k.core.Status.Companion.OK
import swynck.app.Dependencies

class ItemsRoutes(private val dependencies: Dependencies) : HttpHandler {
    override operator fun invoke(request: Request): Response {
        val path = request.uri.toString().split("/").filter { !it.isBlank() }
        val response = GetItems(dependencies, path)
        if (response.status != OK && path.singleOrNull() == "ping") {
            return Response(OK).body("pong")
        }
        return response
    }
}
