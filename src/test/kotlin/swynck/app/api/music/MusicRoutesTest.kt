package swynck.app.api.music

import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.junit.Test
import swynck.dto.onedrive.DeltaResponse
import swynck.dto.onedrive.DriveItem
import swynck.dto.onedrive.FileItem
import swynck.test.utils.*

class MusicRoutesTest {
    private val mimetype = "audio/mpeg"
    private val pageCount = 200
    private val testData = TestData()
    private val dependencies = TestDependencies()
    private val musicRoutes = MusicRoutes(dependencies)
    private val user = testData.user
    private val root = testData.onedrive.rootFolder

    @Test
    fun `ping endpoint works`() {
        assertThat(musicRoutes).hasPingEndpoint()
    }

    @Test
    fun `GetMusic picks up mp3 files`() {
        val file = getNewFile()
        val delta = DeltaResponse(
            null,
            null,
            listOf(root, file)
        )
        dependencies.userRepository.addUser(user)
        dependencies.metadata.insert(delta)

        val result = musicRoutes(Request(GET, "/"))

        assertThat(result).matches { it.status == OK }
        val deserialized = GetMusicResponse.lens(result)
        val fileReturned = deserialized.files.single()
        assertThat(fileReturned).matches { it.name == file.name }
        assertThat(fileReturned).matches { "${user.id}!${it.id}" == file.id }
    }

    @Test
    fun `GetMusic picks up first page of mp3 files alphabetically by filename`() {
        val files = (0..250)
            .map { getNewFile() }
        val delta = DeltaResponse(
            null,
            null,
            listOf(root, *files.toTypedArray())
        )
        dependencies.userRepository.addUser(user)
        dependencies.metadata.insert(delta)

        val result = musicRoutes(Request(GET, "/"))

        assertThat(result).matches { it.status == OK }
        val deserialized = GetMusicResponse.lens(result)
        val filenamesReturned = deserialized.files.map { it.name }
        assertThat(filenamesReturned).isEqualTo(files.map { it.name }.sorted().subList(0, pageCount))
    }

    private fun getNewFile() = DriveItem(
        testData.onedrive.randomDriveItemId(),
        TestData.randomString(),
        FileItem(mimetype),
        null,
        null,
        root.asParentReference()
    )
}