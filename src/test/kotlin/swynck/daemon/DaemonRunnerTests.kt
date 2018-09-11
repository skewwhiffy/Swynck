package swynck.daemon

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.experimental.async
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Before
import org.junit.Test
import swynck.daemon.task.DaemonTask
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class DaemonRunnerTests {
    private var time = Instant.now()
    private val clock = mockk<Clock>()
    private val runner = DaemonRunner(clock)

    @Before
    fun init() {
        every { clock.instant() } answers { time }
    }

    @Test
    fun `runs task at correct interval`() {
        val runs = ConcurrentLinkedQueue<UUID>()
        val task = object : DaemonTask {
            override suspend fun run() {
                runs.add(UUID.randomUUID())
            }

            override val runsPerMinute = 60
        }
        runner.add(task)

        time += Duration.ofMinutes(1)

        var lastRuns = 0
        while (runs.size < 60) {
            println(lastRuns)
            val currentRuns = runs.size
            //if (currentRuns <= lastRuns) fail("I've waited")
            lastRuns = currentRuns
            Thread.sleep(1000)
        }
    }
}