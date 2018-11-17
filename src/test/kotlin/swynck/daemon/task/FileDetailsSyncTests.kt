package swynck.daemon.task

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.h2.jdbcx.JdbcDataSource
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.sql2o.Sql2o
import swynck.config.Config
import swynck.model.User
import swynck.service.Onedrive
import swynck.test.utils.TestConfig
import java.io.File

class FileDetailsSyncTests {
    companion object {
        private var userExists = false
        private lateinit var user: User

        @BeforeClass
        @JvmStatic
        fun initClass() {
            val home = System.getProperty("user.home")
            val file = File("$home/.config/swynck.mv.db")
            if (!file.exists()) {
                println("DB does not exist at ${file.absolutePath}")
                return
            }
            val dataSource = JdbcDataSource().apply { setUrl("jdbc:h2:~/.config/swynck")}
            val sql2o = Sql2o(dataSource)
            sql2o.open().use {
                val users = it
                    .createQuery("SELECT * FROM users")
                    .executeAndFetch(User::class.java)
                if (users.size != 1) {
                    println("More than one refresh token")
                    return
                }
                user = users.single()
                userExists = true
            }
        }
    }

    private lateinit var config: Config
    private lateinit var onedrive: Onedrive

    @Before
    fun init() {
        config = TestConfig()
        onedrive = Onedrive(config)
    }

    @Test
    fun `can get refresh token`() {
        if (!userExists) fail("No user in DB: cannot test")

        val accessToken = onedrive.getAccessToken(user)

        assertThat(accessToken.refresh_token).isNotBlank()
    }

    @Test
    fun `folders are populated`() {
        if (!userExists) fail("No user in DB: cannot test")

    }
}