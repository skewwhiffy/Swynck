package swynck.service

import org.assertj.core.api.Assertions.assertThat
import org.h2.jdbcx.JdbcDataSource
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.sql2o.Sql2o
import swynck.app.Dependencies
import swynck.model.User
import swynck.test.utils.TestDependencies
import java.io.File

class OnedriveAccessTokenTests {
    private val user: User
    private val dependencies: Dependencies

    init {
        val home = System.getProperty("user.home")
        val file = File("$home/.config/swynck/swynck.mv.db")
        if (!file.exists()) {
            assumeTrue("DB does not exist", true)
        }

        val dataSource = JdbcDataSource().apply { setUrl("jdbc:h2:~/.config/swynck/swynck")}
        val sql2o = Sql2o(dataSource)
        user = sql2o.open().use {
            val users = it
                .createQuery("SELECT * FROM users")
                .executeAndFetch(User::class.java)
            assertThat(users).hasSize(1)
            users.single()
        }

        dependencies = TestDependencies()
        dependencies.userRepository.addUser(user)
    }

    @Test
    fun `can get refresh token`() {
        val accessToken = dependencies.oneDrive.getAccessToken(user)

        assertThat(accessToken.access_token).isNotBlank()
    }
}