package swynck.service

import assertk.assert
import assertk.fail
import org.h2.jdbcx.JdbcDataSource
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.sql2o.Sql2o
import swynck.config.Config
import swynck.db.DataSourceFactory
import swynck.db.Migrations
import swynck.db.UserRepository
import swynck.model.User
import swynck.test.utils.TestConfig
import swynck.test.utils.isNotBlank
import java.io.File
import java.net.URI

class OnedriveAccessTokenTests {
    private val user: User
    private val config: Config
    private val onedrive: Onedrive
    private val userRepository: UserRepository

    init {
        val home = System.getProperty("user.home")
        val file = File("$home/.config/swynck/swynck.mv.db")
        if (!file.exists()) {
            assumeTrue("DB does not exist", true)
            fail("DB does not exist at ${file.absolutePath}")
        }

        val dataSource = JdbcDataSource().apply { setUrl("jdbc:h2:~/.config/swynck/swynck")}
        val sql2o = Sql2o(dataSource)
        user = sql2o.open().use {
            val users = it
                .createQuery("SELECT * FROM users")
                .executeAndFetch(User::class.java)
            if (users.size != 1) fail("More than one refresh token not supported")
            users.single()
        }

        config = TestConfig()
        onedrive = Onedrive(config)
        val dataSourceFactory = DataSourceFactory(config)
        Migrations(dataSourceFactory).run()
        userRepository = UserRepository(dataSourceFactory)
        userRepository.addUser(user)
    }

    @Test
    fun `can get refresh token`() {
        val accessToken = onedrive.getAccessToken(user)

        assert(accessToken.access_token).isNotBlank()
    }

    @Test
    fun `can get deltas`() {
        val accessToken = onedrive.getAccessToken(user)

        fun getDeltas(soFar: Int, limit: Int, nextLink: URI?) {
            if (soFar >= limit) return
            val delta = onedrive.getDelta(accessToken, nextLink)
            delta.nextLink?:return
            getDeltas(soFar + delta.value.size, limit, delta.nextLink)
        }
        getDeltas(0, 1000, null)
    }
}