package swynck.daemon.task

import org.assertj.core.api.Assertions.fail
import org.h2.jdbcx.JdbcDataSource
import org.junit.Test
import org.sql2o.Sql2o
import swynck.db.DataSourceFactory
import swynck.db.Migrations
import swynck.db.OnedriveMetadataRepository
import swynck.db.UserRepository
import swynck.model.User
import swynck.service.Onedrive
import swynck.test.utils.TestConfig
import java.io.File
import java.net.URI

class FileDetailsSyncTests {
    private val config = TestConfig()
    private val dataSourceFactory = DataSourceFactory(config)
    private val onedrive = Onedrive(config)
    private val onedriveMetadata = OnedriveMetadataRepository(dataSourceFactory)
    private val user: User

    init {
        val home = System.getProperty("user.home")
        val file = File("$home/.config/swynck/swynck.mv.db")
        if (!file.exists()) {
            fail("DB does not exist at ${file.absolutePath}")
        }
        val dataSource = JdbcDataSource().apply { setUrl("jdbc:h2:~/.config/swynck/swynck") }
        val sql2o = Sql2o(dataSource)
        user = sql2o.open().use {
            val users = it
                .createQuery("SELECT * FROM users")
                .executeAndFetch(User::class.java)
            if (users.size != 1) {
                fail("More than one refresh token")
            }
            users.single()
        }
        Migrations(dataSourceFactory).run()
        UserRepository(dataSourceFactory).addUser(user)
    }

    @Test
    fun `folders are populated`() {
        val accessToken = onedrive.getAccessToken(user)

        var nextLink: URI? = null
        var items = 0
        while (true) {
            val delta = onedrive.getDelta(accessToken, nextLink)
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
            onedriveMetadata.insert(delta)
            if (items > 1000) {
                println("$items items: stopping")
                break
            }
            val firstNonRootItem = delta.value.first { it.name != "root" }.name
            val firstFile = delta.value.firstOrNull { it.file != null }?.name?:"NONE"
            println("$items so far: first folder: $firstNonRootItem first file: $firstFile nextLink: $nextLink deltaLink: ${delta.nextLink}")
        }
    }
}