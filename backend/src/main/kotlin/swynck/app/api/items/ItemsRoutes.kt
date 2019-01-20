package swynck.app.api.items

import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import swynck.app.Dependencies

class ItemsRoutes(private val dependencies: Dependencies) {
    operator fun invoke(path: List<String>): Response {
        val response = GetItems(dependencies, path)
        if (response.status != OK && path.singleOrNull() == "ping") {
            return Response(OK).body("pong")
        }
        return response
    }
}
