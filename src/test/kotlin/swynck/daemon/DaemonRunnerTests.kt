package swynck.daemon

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.experimental.delay
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Test
import swynck.daemon.task.DaemonTask
import swynck.daemon.task.NoRestart
import swynck.daemon.task.Restart
import swynck.daemon.task.RestartPolicy
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class DaemonRunnerTests {
    private var time = Instant.now()
    private val clock = mockk<Clock>()
    private val runner = DaemonRunner()

    init {
        every { clock.instant() } answers { time }
    }

    @Test
    fun `NoRestart stops on exception`() {
        val task = TestDaemonTask(
            NoRestart,
            Duration.ofMillis(5)
        )

        runner.add(task)

        task.waitForRuns(100)

        task.blowUpNextRun()

        while (!task.exceptionsThrown.any()) {
            Thread.sleep(100)
        }
        val status = runner.statusOf(task) as Errored
        assertThat(status.exceptions.single()).isEqualTo(task.exceptionsThrown.single())
    }

    @Test
    fun `Restart restarts on exception after pause`() {
        val task = TestDaemonTask(
            Restart(Duration.ofMinutes(1)),
            Duration.ofMillis(5)
        )

        runner.add(task)

        task.waitForRuns(100)

        task.blowUpNextRun()

        while (!task.exceptionsThrown.any()) {
            Thread.sleep(100)
        }
        val status = runner.statusOf(task) as RunningWithErrors
        assertThat(status.exceptions).isEqualTo(task.exceptionsThrown)
    }

    private fun TestDaemonTask.waitForRuns(totalRuns: Int) {
        var lastRuns = 0
        while (numberOfRuns < totalRuns) {
            val currentRuns = numberOfRuns
            assumeTrue("I expected $currentRuns to be larger than $lastRuns", lastRuns > currentRuns)
            lastRuns = currentRuns
            Thread.sleep(100)
        }
    }

    private class TestDaemonTask(
        override val restartPolicy: RestartPolicy,
        private val delayBetweenRuns: Duration
    ) : DaemonTask {
        private var blowUp = false
        private val exceptions = ConcurrentHashMap<UUID, Exception>()
        private val runs = ConcurrentLinkedQueue<UUID>()

        override suspend fun runSingle() {
            runs.add(UUID.randomUUID())
            delay(delayBetweenRuns.toMillis())
            if (blowUp) {
                throw Exception("${UUID.randomUUID()}")
                    .also { exceptions[UUID.randomUUID()] = it }
            }
        }

        fun blowUpNextRun() {
            blowUp = true
        }

        val exceptionsThrown get() = exceptions.values.toList()

        val numberOfRuns get() = runs.size
    }
}