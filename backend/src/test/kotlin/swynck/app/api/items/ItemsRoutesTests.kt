package swynck.app.api.items

import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.junit.Test
import swynck.real.onedrive.dto.*
import swynck.test.utils.TestData
import swynck.test.utils.TestDependencies
import swynck.test.utils.asParentReference
import swynck.test.utils.hasPingEndpoint

class ItemsRoutesTests {
    private val testData = TestData()
    private val dependencies = TestDependencies()
    private val itemRoutes = ItemsRoutes(dependencies)

    @Test
    fun `ping endpoint works`() {
        assertThat(itemRoutes).hasPingEndpoint()
    }

    @Test
    fun `get items returns files`() {
        dependencies.userRepository.addUser(testData.user)
        val filenames = (0..10).map { TestData.randomString() }
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

        val result = itemRoutes(Request(Method.GET, "/"))

        assertThat(result.status).isEqualTo(OK)
        val deserializedResponse = GetItemsResponse.lens(result)
        assertThat(deserializedResponse.files.map { it.name }).isEqualTo(filenames)
    }

    @Test
    fun `get items returns files is subfolder`() {
        dependencies.userRepository.addUser(testData.user)
        val folderNames = (0..10).map { TestData.randomString() }
        val folders = folderNames
            .fold(listOf<DriveItem>()) { c, name -> c + DriveItem(
                testData.onedrive.randomDriveItemId(),
                name,
                null,
                FolderItem(1),
                null,
                (c.lastOrNull()?:testData.onedrive.rootFolder).asParentReference()
            )}
            .toTypedArray()
            .let { listOf(testData.onedrive.rootFolder, *it) }
        val fileNames = (0..10).map { TestData.randomString() }
        val containingFolder = folders[folders.size - 2]
        val files = fileNames
            .map { name -> DriveItem(
                testData.onedrive.randomDriveItemId(),
                name,
                FileItem(null),
                null,
                null,
                containingFolder.asParentReference()
            )}
        val deltaResponse = DeltaResponse(
            null,
            null,
            folders + files
        )
        dependencies.metadata.insert(deltaResponse)
        val path = folderNames
            .subList(0, folderNames.size - 1)
            .joinToString("/")

        val result = itemRoutes(Request(Method.GET, path))

        assertThat(result.status).isEqualTo(OK)
        val deserializedResponse = GetItemsResponse.lens(result)
        assertThat(deserializedResponse.files.map { it.name }).isEqualTo(fileNames)
        assertThat(deserializedResponse.folders.map { it.name }).isEqualTo(listOf(folderNames.last()))
    }
}