package swynck.daemon

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.experimental.delay
import org.assertj.core.api.Assertions.fail
import org.junit.Before
import org.junit.Test
import swynck.daemon.task.DaemonTask
import java.time.Clock
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
    fun `runs task continuously`() {
        val runs = ConcurrentLinkedQueue<UUID>()
        val task = object : DaemonTask {
            override suspend fun runSingle() {
                runs.add(UUID.randomUUID())
                delay(5)
            }
        }
        runner.add(task)

        var lastRuns = 0
        while (runs.size < 100) {
            println(lastRuns)
            val currentRuns = runs.size
            if (lastRuns > currentRuns) fail("I expected $currentRuns to be larger than $lastRuns")
            lastRuns = currentRuns
            Thread.sleep(100)
        }
    }
}