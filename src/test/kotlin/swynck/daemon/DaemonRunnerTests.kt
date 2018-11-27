package swynck.daemon

import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant

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
        /*
        val runs = ConcurrentLinkedQueue<UUID>()
        val task = object : DaemonTask {
            override suspend fun run(): Nothing {
                runs.add(UUID.randomUUID())
            }
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
        */
    }
}