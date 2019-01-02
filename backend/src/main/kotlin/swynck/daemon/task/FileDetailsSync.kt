package swynck.daemon.task

import swynck.app.Dependencies
import swynck.common.model.User
import java.time.Duration

class FileDetailsSync(
    val user: User,
    val dependencies: Dependencies
) : DaemonTask {
    companion object {
        private val updatingDelay = Duration.ofSeconds(1)!!
        private val pollingInterval = Duration.ofSeconds(15)!!
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
                // TODO: Logging
                println("Next link returned: $it")
                dependencies.ticker.delay(updatingDelay)
            }
            delta.deltaLink?.let {
                dependencies.userRepository.setNextLink(user, it)
                println("Delta link returned: $it")
                dependencies.ticker.delay(pollingInterval)
            }
        } catch (e: Exception) {
            println(e)
            e.printStackTrace()
            dependencies.ticker.delay(pollingInterval)
        }
    }

    override val restartPolicy = NoRestart
}
