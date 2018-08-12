package swynck.daemon.task

import swynck.model.User

class FileDetailsSync(val user: User) : DaemonTask {
    override suspend fun run() {
        TODO()
    }
}