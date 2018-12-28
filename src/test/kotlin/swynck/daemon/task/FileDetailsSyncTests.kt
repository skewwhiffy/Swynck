package swynck.daemon.task

import org.assertj.core.api.Assertions.assertThat
import org.h2.jdbcx.JdbcDataSource
import org.junit.Test
import org.sql2o.Sql2o
import swynck.model.User
import swynck.test.utils.TestDependencies
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
        var items = 0
        while (true) {
            val delta = dependencies.oneDrive.getDelta(accessToken, nextLink)
            if (nextLink == delta.nextLink) {
                println("Next link has not changed")
                break
            }
            nextLink = delta.nextLink
            if (nextLink == null) {
                println("Next link is null")
                println("Delta link is ${delta.deltaLink}")
                break
            }
            items += delta.value.size
            dependencies.metadata.insert(delta)
            if (items > 500) {
                println("$items items: stopping")
                break
            }
            val firstNonRootItem = delta.value.first { it.name != "root" }.name
            val firstFile = delta.value.firstOrNull { it.file != null }?.name?:"NONE"
            println("$items so far: first folder: $firstNonRootItem first file: $firstFile nextLink: $nextLink deltaLink: ${delta.nextLink}")
        }
    }
}