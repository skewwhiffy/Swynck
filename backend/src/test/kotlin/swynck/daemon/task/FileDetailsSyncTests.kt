package swynck.daemon.task

import kotlinx.coroutines.runBlocking
import org.junit.Test
import swynck.common.model.User
import swynck.test.utils.TestDependencies
import swynck.util.executeAndFetch

class FileDetailsSyncTests {
    private val dependencies = TestDependencies()
    private val user: User
    private val sut: FileDetailsSync

    init {
        dependencies.addValidUser()
        user = dependencies.userRepository.getUser()!!
        sut = FileDetailsSync(user, dependencies)
    }

    @Test
    fun `folders are populated`() {
        while (!dependencies.onedriveClients.hasDeltaLinkBeenRequested) {
            runBlocking { sut.runSingle() }
            dependencies.dataSourceFactory.sql2o().use {
                val fileCount = "SELECT COUNT(*) FROM files"
                    .let(it::createQuery)
                    .executeAndFetch<Int>()
                    .single()
                println("File count is $fileCount")
            }
        }
    }
}