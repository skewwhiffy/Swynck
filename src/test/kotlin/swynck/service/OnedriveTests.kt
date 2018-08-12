package swynck.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.h2.jdbcx.JdbcDataSource
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.sql2o.Sql2o
import swynck.config.Config
import swynck.db.DataSourceFactory
import swynck.db.Migrations
import swynck.db.UserRepository
import swynck.model.User
import swynck.test.utils.TestConfig
import java.io.File
import java.net.PortUnreachableException

class OnedriveAccessTokenTests {
    companion object {
        private lateinit var user: User

        @BeforeClass
        @JvmStatic
        fun initClass() {
            val home = System.getProperty("user.home")
            val file = File("$home/.config/swynck.mv.db")
            if (!file.exists()) {
                assumeTrue("DB does not exist", true)
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
                    assumeFalse("More than one refresh token not supported", true)
                    return
                }
                user = users.single()
            }
        }
    }

    private lateinit var config: Config
    private lateinit var onedrive: Onedrive
    private lateinit var userRepository: UserRepository

    @Before
    fun init() {
        config = TestConfig()
        onedrive = Onedrive(config)
        val dataSourceFactory = DataSourceFactory(config)
        Migrations(dataSourceFactory).run()
        userRepository = UserRepository(dataSourceFactory)
        userRepository.addUser(user)
        print(user.redirectUri)
    }

    @Test
    fun `can get refresh token`() {
        val accessToken = onedrive.getAccessToken(user)

        assertThat(accessToken.refresh_token).isEqualTo(user.refreshToken)
    }
}

class OnedriveAuthenticationTests {
    private lateinit var config: TestConfig
    private lateinit var onedrive: Onedrive

    @Before
    fun init() {
        config = TestConfig()
        onedrive = Onedrive(config)
    }

    @Test
    fun `when using unsupported callback port then authentication URL blows up`() {
        config.port = 0

        assertThatThrownBy { onedrive.authenticationUrl() }.isInstanceOf(PortUnreachableException::class.java)
    }

    @Test
    fun `when using supported callback port then authentication URL is returned`() {
        config.port = 38080

        val url = onedrive.authenticationUrl()

        val queryVars = url.query!!
        println(queryVars)
    }
}