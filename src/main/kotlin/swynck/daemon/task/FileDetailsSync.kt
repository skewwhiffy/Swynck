package swynck.daemon.task

import kotlinx.coroutines.experimental.delay
import swynck.app.Dependencies
import swynck.db.OnedriveMetadataRepository
import swynck.db.UserRepository
import swynck.model.User
import swynck.service.Onedrive

class FileDetailsSync(
    val user: User,
    val userRepository: UserRepository,
    val onedrive: Onedrive,
    private val metadata: OnedriveMetadataRepository
) : DaemonTask {
    constructor(user: User, dependencies: Dependencies) : this(
        user,
        dependencies.userRepository,
        dependencies.oneDrive,
        dependencies.metadata
    )
    override suspend fun run(): Nothing {
        while (true) {
            try {
                val nextLink = userRepository.getNextLink(user)
                val accessToken = onedrive.getAccessToken(user)
                println("Getting delta with next link: $nextLink")
                val delta = onedrive.getDelta(accessToken, nextLink)
                println("Delta returned with ${delta.value.size} items")
                metadata.insert(delta)
                delta.nextLink?.let { userRepository.setNextLink(user, it) }
                println("Next link returned: ${delta.nextLink}")
                delay(1000)
            } catch (e: Exception) {
                println(e)
                e.printStackTrace()
                delay(5000)
            }
        }
    }
}
