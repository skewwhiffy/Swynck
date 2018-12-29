package swynck.daemon.task

import kotlinx.coroutines.delay
import swynck.app.Dependencies
import swynck.model.User
import java.time.Duration

class FileDetailsSync(
    val user: User,
    val dependencies: Dependencies
) : DaemonTask {
    companion object {
        private val updatingDelay = Duration.ofSeconds(1)!!
        private val pollingInterval = Duration.ofSeconds(15)!!
        private suspend fun delay(duration: Duration) = delay(duration.toMillis())
    }

    override suspend fun runSingle() {
        try {
            val nextLink = dependencies.userRepository.getNextLink(user)
            val accessToken = dependencies.oneDrive.getAccessToken(user)
            val delta = dependencies.oneDrive.getDelta(accessToken, nextLink)
            println("Delta returned with ${delta.value.size} items")
            dependencies.metadata.insert(delta)
            delta.nextLink?.let {
                dependencies.userRepository.setNextLink(user, it)
                println("Next link returned: $it")
                delay(updatingDelay)
            }
            delta.deltaLink?.let {
                dependencies.userRepository.setNextLink(user, it)
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
