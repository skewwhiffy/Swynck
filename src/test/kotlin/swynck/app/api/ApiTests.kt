package swynck.app.api

import assertk.assert
import assertk.assertions.isEqualTo
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.junit.Test
import swynck.dto.onedrive.*
import swynck.test.utils.TestData
import swynck.test.utils.TestDependencies
import swynck.test.utils.asParentReference

class ApiTests {
    private val testData = TestData()
    private val dependencies = TestDependencies()
    private val api = Api(dependencies)

    @Test
    fun `ping endpoint works`() {
        val result = api(Request(Method.GET, "/ping"))

        assert(result.status).isEqualTo(OK)
        assert(result.bodyString()).isEqualTo("pong")
    }

    @Test
    fun `ItemsRoutes is wired in correctly`() {
        val result = api(Request(Method.GET, "/items/ping"))

        assert(result.status).isEqualTo(OK)
        assert(result.bodyString()).isEqualTo("pong")
    }

    @Test
    fun `get items returns files`() {
        dependencies.userRepository.addUser(testData.user)
        val filenames = (0..10)
            .map { TestData.randomString() }
        val files = filenames
            .map { DriveItem(
                testData.onedrive.randomDriveItemId(),
                it,
                FileItem(TestData.randomString()),
                null,
                null,
                testData.onedrive.rootFolder.asParentReference()
            ) }
        val driveItems = listOf(testData.onedrive.rootFolder, *files.toTypedArray())
        val deltaResponse = DeltaResponse(
            null,
            null,
            driveItems
        )
        dependencies.metadata.insert(deltaResponse)

        val result = api(Request(Method.GET, "/items"))

        assert(result.status).isEqualTo(OK)
        val deserializedResponse = GetItemsResponse.lens(result)
        assert(deserializedResponse.files.map { it.name })
            .isEqualTo(filenames)
    }
}