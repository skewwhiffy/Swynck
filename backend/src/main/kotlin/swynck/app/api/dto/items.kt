package swynck.app.api.dto

import swynck.db.File
import swynck.db.Folder

data class FolderDto(
    val name: String
)

data class FileDto(
    val id: Int,
    val name: String,
    val mime: String?
)

fun Folder.toDto() = FolderDto(name)
fun File.toDto() = FileDto(id, name, mimeType)
