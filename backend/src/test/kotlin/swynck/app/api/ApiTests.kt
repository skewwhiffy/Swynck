package swynck.app.api

import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.junit.Test
import swynck.app.api.items.GetItemsResponse
import swynck.real.onedrive.dto.*
import swynck.test.utils.TestData
import swynck.test.utils.TestDependencies
import swynck.test.utils.asParentReference
import swynck.test.utils.hasPingEndpoint

class ApiTests {
    private val testData = TestData()
    private val dependencies = TestDependencies()
    private val api = Api(dependencies)

    @Test
    fun `ping endpoint works`() {
        assertThat(api).hasPingEndpoint()
    }

    @Test
    fun `ItemsRoutes is wired in correctly`() {
        assertThat(api).hasPingEndpoint("/items/ping")
    }

    @Test
    fun `MusicRoutes is wired in correctly`() {
        assertThat(api).hasPingEndpoint("/music/ping")
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

        assertThat(result.status).isEqualTo(OK)
        val deserializedResponse = GetItemsResponse.lens(result)
        assertThat(deserializedResponse.files.map { it.name })
            .isEqualTo(filenames)
    }
}