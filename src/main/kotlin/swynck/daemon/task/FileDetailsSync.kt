package swynck.daemon.task

import kotlinx.coroutines.experimental.delay
import swynck.app.Dependencies
import swynck.db.OnedriveMetadataRepository
import swynck.db.UserRepository
import swynck.model.User
import swynck.service.Onedrive
import java.time.Duration
import java.util.concurrent.TimeUnit

class FileDetailsSync(
    val user: User,
    val userRepository: UserRepository,
    val onedrive: Onedrive,
    private val metadata: OnedriveMetadataRepository
) : DaemonTask {
    companion object {
        private val updatingDelay = Duration.ofSeconds(1)!!
        private val pollingInterval = Duration.ofSeconds(15)!!
        private suspend fun delay(duration: Duration) = delay(duration.toNanos(), TimeUnit.NANOSECONDS)
    }
    constructor(user: User, dependencies: Dependencies) : this(
        user,
        dependencies.userRepository,
        dependencies.oneDrive,
        dependencies.metadata
    )

    override suspend fun runSingle() {
        try {
            val nextLink = userRepository.getNextLink(user)
            val accessToken = onedrive.getAccessToken(user)
            val delta = onedrive.getDelta(accessToken, nextLink)
            println("Delta returned with ${delta.value.size} items")
            metadata.insert(delta)
            delta.nextLink?.let {
                userRepository.setNextLink(user, it)
                println("Next link returned: $it")
                delay(updatingDelay)
            }
            delta.deltaLink?.let {
                userRepository.setNextLink(user, it)
                println("Delta link returned: $it")
                delay(pollingInterval)
            }
        } catch (e: Exception) {
            println(e)
            e.printStackTrace()
            delay(pollingInterval)
        }
    }

    override val restartPolicy = NoRestart
}
