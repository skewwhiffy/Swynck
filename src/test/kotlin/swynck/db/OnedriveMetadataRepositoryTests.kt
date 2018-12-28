package swynck.db

import assertk.assert
import assertk.assertions.isEqualTo
import org.junit.Test
import swynck.dto.onedrive.*
import swynck.model.User
import swynck.test.utils.TestConfig
import swynck.test.utils.TestData
import java.util.*

class OnedriveMetadataRepositoryTests {
    private val dataSourceFactory = TestConfig().let(::DataSourceFactory)
    private val metadataRepository = OnedriveMetadataRepository(dataSourceFactory)
    private val userRepository = UserRepository(dataSourceFactory)
    private val user: User

    companion object {
        private var lastId = 0
        fun getNextId() = ++lastId
    }

    init {
        Migrations(dataSourceFactory).run()
        user = TestData
            .randomUser()
            .also(userRepository::addUser)
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
        assert(folder).isEqualTo(Folder(id, "root"))
    }

    @Test
    fun `can update folder`() {
        val rootFolderDriveItem = getRootFolderDriveItem(getNextId())
        val childId = getNextId()
        val childFolder = getSubFolderDriveItem(rootFolderDriveItem, childId)
        val newChildFolder = getSubFolderDriveItem(rootFolderDriveItem, childId)
        val delta = DeltaResponse(
            null,
            null,
            listOf(rootFolderDriveItem, childFolder)
        )
        val secondDelta = DeltaResponse(
            null,
            null,
            listOf(rootFolderDriveItem, newChildFolder)
        )

        metadataRepository.insert(delta)
        metadataRepository.insert(secondDelta)

        val rootFolder = metadataRepository.getRootFolder(user)
        val folder = metadataRepository.getFolders(user, rootFolder).single()
        assert(folder).isEqualTo(Folder(childId, newChildFolder.name))
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
        assert(childFoldersReturned.map { it.name }.sorted()).isEqualTo(childFolders.map { it.name }.sorted())
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
        assert(getChildFoldersReturned().map { it.name }).isEqualTo(childFolders.map { it.name })
    }

    @Test
    fun `can insert file in root`() {
        val rootFolder = getRootFolderDriveItem(getNextId())
        val file = getFileDriveItem(rootFolder, getNextId())
        val delta = DeltaResponse(null, null, listOf(rootFolder, file))

        metadataRepository.insert(delta)

        val rootFolderReturned = metadataRepository.getRootFolder(user)
        val fileReturned = metadataRepository.getFiles(user, rootFolderReturned).single()
        assert(fileReturned).isEqualTo(File(file.id.split("!")[1].toInt(), file.name, file.file!!.mimeType))
    }

    @Test
    fun `can update file`() {
        val rootFolder = getRootFolderDriveItem(getNextId())
        val fileId = getNextId()
        val file = getFileDriveItem(rootFolder, fileId)
        val newFile = getFileDriveItem(rootFolder, fileId)
        val delta = DeltaResponse(null, null, listOf(rootFolder, file))
        val secondDelta = DeltaResponse(null, null, listOf(rootFolder, newFile))

        metadataRepository.insert(delta)
        metadataRepository.insert(secondDelta)

        val rootFolderReturned = metadataRepository.getRootFolder(user)
        val fileReturned = metadataRepository.getFiles(user, rootFolderReturned).single()
        assert(fileReturned).isEqualTo(File(fileId, newFile.name, newFile.file!!.mimeType))
    }

    private fun getRootFolderDriveItem(id: Int) = DriveItem(
        "${user.id.toUpperCase()}!$id",
        "root",
        null,
        FolderItem(0),
        null,
        ParentReference(user.id, "${user.id.toUpperCase()}!0")
    )

    private fun getSubFolderDriveItem(parentDriveItem: DriveItem, id: Int) = DriveItem(
        "${user.id.toUpperCase()}!$id",
        "${UUID.randomUUID()}",
        null,
        FolderItem(0),
        null,
        ParentReference(user.id, "${user.id.toUpperCase()}!${parentDriveItem.id.split("!")[1].toInt()}")
    )

    private fun getFileDriveItem(parentDriveItem: DriveItem, id: Int) = DriveItem(
        "${user.id.toUpperCase()}!$id",
        "${UUID.randomUUID()}",
        FileItem("${UUID.randomUUID()}"),
        null,
        null,
        ParentReference(user.id, "${user.id.toUpperCase()}!${parentDriveItem.id.split("!")[1].toInt()}")
    )
}