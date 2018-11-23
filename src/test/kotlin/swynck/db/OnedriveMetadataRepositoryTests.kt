package swynck.db

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import swynck.dto.onedrive.*
import swynck.model.User
import swynck.test.utils.TestConfig
import java.util.*

class OnedriveMetadataRepositoryTests {
    private lateinit var dataSourceFactory: DataSourceFactory
    private lateinit var metadataRepository: OnedriveMetadataRepository
    private lateinit var userRepository: UserRepository
    private lateinit var user: User

    companion object {
        private var lastId = 0
        fun getNextId() = ++lastId
    }

    @Before
    fun init() {
        val config = TestConfig()
        dataSourceFactory = DataSourceFactory(config)
        Migrations(dataSourceFactory).run()
        metadataRepository = OnedriveMetadataRepository(dataSourceFactory)
        userRepository = UserRepository(dataSourceFactory)
        user = UserRepositoryTests.newUser()
        userRepository.addUser(user)
    }

    @Test
    fun `can insert root folder`() {
        val id = getNextId()
        val delta = DeltaResponse(
            null,
            null,
            listOf(getRootFolderDriveItem(id))
        )

        metadataRepository.insert(delta)

        val folder = metadataRepository.getRootFolder(user)
        assertThat(folder).isEqualTo(Folder(id, "root"))
    }

    @Test
    fun `can insert folders inside root`() {
        val rootFolder = getRootFolderDriveItem(getNextId())
        val childFolders = (0..100)
            .map { getSubFolderDriveItem(rootFolder, getNextId()) }
        val folders = listOf(rootFolder, *childFolders.toTypedArray())
        val delta = DeltaResponse(null, null, folders)

        metadataRepository.insert(delta)

        val rootFolderReturned = metadataRepository.getRootFolder(user)
        val childFoldersReturned = metadataRepository.getFolders(user, rootFolderReturned)
        assertThat(childFoldersReturned.map { it.name }.sorted()).isEqualTo(childFolders.map { it.name }.sorted())
    }

    @Test
    fun `can insert folders to arbitrary depth`() {
        val rootFolder = getRootFolderDriveItem(getNextId())
        tailrec fun getChildFolders(prev: List<DriveItem> = listOf()): List<DriveItem> {
            return if (prev.size > 100) prev
            else getChildFolders(prev + getSubFolderDriveItem(
                prev.lastOrNull() ?: rootFolder,
                getNextId()
            ))
        }
        val childFolders = getChildFolders()
        val folders = listOf(rootFolder, *childFolders.toTypedArray())
        val delta = DeltaResponse(null, null, folders)

        metadataRepository.insert(delta)

        val rootFolderReturned = metadataRepository.getRootFolder(user)
        fun getChildFoldersReturned(prev: List<Folder> = listOf()): List<Folder> = metadataRepository
            .getFolders(user, prev.lastOrNull() ?: rootFolderReturned)
            .singleOrNull()
            ?.let { getChildFoldersReturned(prev + it) }
            ?:prev
        assertThat(getChildFoldersReturned().map { it.name }).isEqualTo(childFolders.map { it.name })
    }

    @Test
    fun `can insert file in root`() {
        val rootFolder = getRootFolderDriveItem(getNextId())
        val file = getFileDriveItem(rootFolder, getNextId())
        val delta = DeltaResponse(null, null, listOf(rootFolder, file))

        metadataRepository.insert(delta)

        val rootFolderReturned = metadataRepository.getRootFolder(user)
        val fileReturned = metadataRepository.getFiles(user, rootFolderReturned).single()
        assertThat(fileReturned).isEqualTo(File(file.id.split("!")[1].toInt(), file.name, file.file!!.mimeType))
    }

    private fun getRootFolderDriveItem(id: Int) = DriveItem(
        "${user.id.toUpperCase()}!$id",
        "root",
        null,
        FolderItem(0),
        ParentReference(user.id, "${user.id.toUpperCase()}!0")
    )

    private fun getSubFolderDriveItem(parentDriveItem: DriveItem, id: Int) = DriveItem(
        "${user.id.toUpperCase()}!$id",
        "${UUID.randomUUID()}",
        null,
        FolderItem(0),
        ParentReference(user.id, "${user.id.toUpperCase()}!${parentDriveItem.id.split("!")[1].toInt()}")
    )

    private fun getFileDriveItem(parentDriveItem: DriveItem, id: Int) = DriveItem(
        "${user.id.toUpperCase()}!$id",
        "${UUID.randomUUID()}",
        FileItem("${UUID.randomUUID()}"),
        null,
        ParentReference(user.id, "${user.id.toUpperCase()}!${parentDriveItem.id.split("!")[1].toInt()}")
    )
}