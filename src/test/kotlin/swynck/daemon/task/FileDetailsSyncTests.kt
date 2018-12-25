package swynck.daemon.task

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.h2.jdbcx.JdbcDataSource
import org.junit.Test
import org.sql2o.Sql2o
import swynck.model.User
import swynck.service.Onedrive
import swynck.test.utils.TestConfig
import java.io.File

class FileDetailsSyncTests {
    private val config = TestConfig()
    private val onedrive = Onedrive(config)
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
    }

    @Test
    fun `can get refresh token`() {
        val accessToken = onedrive.getAccessToken(user)

        assertThat(accessToken.refresh_token).isNotBlank()
    }

    @Test
    fun `folders are populated`() {
        // TODO
    }
}