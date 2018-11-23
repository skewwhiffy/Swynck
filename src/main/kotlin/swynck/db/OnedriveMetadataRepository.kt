package swynck.db

import swynck.dto.onedrive.DeltaResponse
import swynck.dto.onedrive.DriveItem
import swynck.model.User
import swynck.util.executeAndFetch

class OnedriveMetadataRepository(private val dataSourceFactory: DataSourceFactory) {
    fun insert(delta: DeltaResponse) {
        fun DriveItem.toFolderDao(): FolderDao {
            val userId = parentReference.driveId
            val id = this.id.split("!")[1].toInt()
            val parentFolder = parentReference.id.split("!")[1].toInt()
            return FolderDao(
                id,
                userId,
                name,
                if (parentFolder == 0) null else parentFolder
            )
        }

        val folders = delta
            .value
            .filter { it.folder != null }
            .map { it.toFolderDao() }
        if (folders.size != delta.value.size) throw IllegalArgumentException("Some delta items not accounted for")
        dataSourceFactory.sql2o().use { c ->
            folders
                .map {
                    """
                    INSERT INTO folders (id, userId, name, parentFolder) VALUES (:id, :userId, :name, :parentFolder)
                """.trimIndent()
                        .let(c::createQuery)
                        .bind(it)
                }
                .forEach { it.executeUpdate() }
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
