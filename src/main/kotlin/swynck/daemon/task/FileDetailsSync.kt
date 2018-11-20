package swynck.daemon.task

import swynck.model.User
import swynck.service.Onedrive

class FileDetailsSync(
    val user: User,
    val onedrive: Onedrive
) : DaemonTask {
    override suspend fun run() {
        println("File details SYNC")
    }

    override val runsPerMinute = 10
}