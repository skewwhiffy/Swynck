package swynck.daemon.task

import org.assertj.core.api.Assertions.assertThat
import org.h2.jdbcx.JdbcDataSource
import org.junit.Test
import org.sql2o.Sql2o
import swynck.common.model.User
import swynck.test.utils.TestDependencies
import swynck.util.executeAndFetch
import java.io.File
import java.net.URI

class FileDetailsSyncTests {
    private val dependencies = TestDependencies()
    private val user: User

    init {
        val home = System.getProperty("user.home")
        val file = File("$home/.config/swynck/swynck.mv.db")
        assertThat(file).matches { it.exists() }
        val dataSource = JdbcDataSource().apply { setUrl("jdbc:h2:~/.config/swynck/swynck") }
        val sql2o = Sql2o(dataSource)
        user = sql2o.open().use {
            val users = it
                .createQuery("SELECT * FROM users")
                .executeAndFetch(User::class.java)
            assertThat(users).hasSize(1)
            users.single()
        }
        dependencies.userRepository.addUser(user)
    }

    @Test
    fun `folders are populated`() {
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
                if (fileCount > 1000) {
                    println("File count is large. Exiting.")
                    return
                }
            }
        }
    }
}