package swynck.daemon.task

import org.junit.Test
import swynck.test.utils.TestDependencies
import swynck.util.executeAndFetch
import java.net.URI

class FileDetailsSyncTests {
    private val dependencies = TestDependencies()

    @Test
    fun `folders are populated`() {
        dependencies.addValidUser()
        val user = dependencies.userRepository.getUser()!!
        val accessToken = dependencies.oneDrive.getAccessToken(user)

        var nextLink: URI? = null

        while (true) {
            val delta = dependencies.oneDrive.getDelta(accessToken, nextLink)
            if (nextLink == delta.nextLink) {
                println("Next link has not changed")
                return
            }
            nextLink = delta.nextLink
            if (nextLink == null) {
                println("Next link is null")
                println("Delta link is ${delta.deltaLink}")
                return
            }
            dependencies.metadata.insert(delta)
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