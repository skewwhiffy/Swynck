package swynck.test.utils

import swynck.real.onedrive.dto.*
import swynck.common.model.User
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class TestData {
    companion object {
        private val nextUniqueInt = AtomicInteger(1)

        fun randomUser() = User(
            randomString(),
            randomString(),
            randomString(),
            randomString()
        )

        fun randomString() = "${UUID.randomUUID()}"

        fun uniqueInt() = nextUniqueInt.getAndIncrement()
    }

    val user = User(
        randomString(),
        randomString(),
        randomString(),
        randomString()
    )

    val onedrive = Onedrive(user)

    class Onedrive(user: User) {
        private val driveId = user.id
        fun randomDriveItemId() = "$driveId!${uniqueInt()}"

        val rootFolder = DriveItem(
            randomDriveItemId(),
            "root",
            null,
            FolderItem(0),
            null,
            ParentReference(
                driveId,
                "$driveId!0"
            )
        )
    }
}

fun DriveItem.asParentReference() = ParentReference(
    parentReference.driveId,
    id
)