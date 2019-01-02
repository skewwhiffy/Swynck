package swynck.real.onedrive.dto

data class DriveItem(
        val id: String,
        val name: String,
        val file: FileItem?,
        val folder: FolderItem?,
        val `package`: PackageItem?,
        val parentReference: ParentReference
)

data class FileItem(
        val mimeType: String
)

data class FolderItem(
        val childCount: Int
)

data class PackageItem(
        val type: String
)

data class ParentReference(
        val driveId: String,
        val id: String
)
