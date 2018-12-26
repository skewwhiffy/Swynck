package swynck.db

import swynck.dto.onedrive.DeltaResponse
import swynck.dto.onedrive.DriveItem
import swynck.model.User
import swynck.util.executeAndFetch

class OnedriveMetadataRepository(private val dataSourceFactory: DataSourceFactory) {
    fun insert(delta: DeltaResponse) {
        val folders = delta
            .value
            .filter { it.folder != null || it.`package` != null }
            .map { it.toFolderDao() }
        val files = delta
            .value
            .filter { it.file != null }
            .map { it.toFileDao() }
        if (folders.size + files.size != delta.value.size) {
            val example = delta
                .value
                .filter { it.folder == null }
                .firstOrNull { it.file == null }
            throw IllegalArgumentException("Some delta items not accounted for: $example")
        }
        dataSourceFactory.sql2o().use { c ->
            val folderQuery = """
MERGE INTO folders (id, userId, name, parentFolder) KEY (id) VALUES (:id, :userId, :name, :parentFolder)
            """.trimIndent()
                .let(c::createQuery)
            folders.forEach { folderQuery.bind(it).addToBatch() }
            folderQuery.executeBatch()

            val fileQuery = """
MERGE INTO files (id, userId, name, mimeType, folder) KEY(id) VALUES (:id, :userId, :name, :mimeType, :folder)
            """.trimIndent()
                .let(c::createQuery)
            files.forEach { fileQuery.bind(it).addToBatch() }
            fileQuery.executeBatch()
        }
    }

    fun getRootFolder(user: User) = dataSourceFactory
        .sql2o()
        .use {
            "SELECT * FROM folders WHERE userId = :userId AND parentFolder IS NULL"
                .let(it::createQuery)
                .addParameter("userId", user.id)
                .executeAndFetch<FolderDao>()
                .single()
        }
        .let { Folder(it.id, it.name) }

    fun getFolders(user: User, parentFolder: Folder) = dataSourceFactory
        .sql2o()
        .use {
            "SELECT * FROM folders WHERE userId = :userId AND parentFolder = :parentFolder"
                .let(it::createQuery)
                .addParameter("userId", user.id)
                .addParameter("parentFolder", parentFolder.id)
                .executeAndFetch<FolderDao>()
        }
        .map { Folder(it.id, it.name) }

    fun getFiles(user: User, folder: Folder): List<File> = dataSourceFactory
        .sql2o()
        .use {
            "SELECT * FROM files WHERE userId = :userId AND folder = :folder"
                .let(it::createQuery)
                .addParameter("userId", user.id)
                .addParameter("folder", folder.id)
                .executeAndFetch<FileDao>()
        }
        .map { File(it.id, it.name, it.mimeType) }

    private fun DriveItem.toFolderDao() = FolderDao(
        getId(),
        getUserId(),
        name,
        getParentFolder()
    )

    private fun DriveItem.toFileDao() = FileDao(
        getId(),
        getUserId(),
        name,
        file!!.mimeType,
        getParentFolder()!!
    )

    private fun DriveItem.getUserId() = parentReference.driveId

    private fun DriveItem.getId() = id.extractId()

    private fun DriveItem.getParentFolder() = parentReference
        .id
        .extractId()
        .let { if (it == 0) null else it }

    private fun String.extractId() = split("!")[1].toInt()
}

private data class FolderDao(
    val id: Int,
    val userId: String,
    val name: String,
    val parentFolder: Int?
)

private data class FileDao(
    val id: Int,
    val userId: String,
    val name: String,
    val mimeType: String,
    val folder: Int
)

data class Folder(
    val id: Int,
    val name: String
)

data class File(
    val id: Int,
    val name: String,
    val mimeType: String
)
