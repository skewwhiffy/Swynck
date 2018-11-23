package swynck.db

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import swynck.model.User
import swynck.test.utils.TestConfig
import java.net.URI
import java.util.*

class UserRepositoryTests {
    private lateinit var dataSourceFactory: DataSourceFactory
    private lateinit var userRepository: UserRepository
    companion object {
        fun newUser() = User(
            "${UUID.randomUUID()}",
            "${UUID.randomUUID()}",
            "${UUID.randomUUID()}",
            "${UUID.randomUUID()}"
        )
    }

    @Before
    fun init() {
        val config = TestConfig()
        dataSourceFactory = DataSourceFactory(config)
        Migrations(dataSourceFactory).run()
        userRepository = UserRepository(dataSourceFactory)
    }

    @Test
    fun `get user with no user returns null`() {
        val user = userRepository.getUser()

        assertThat(user).isNull()
    }

    @Test
    fun `add user then get user returns same user`() {
        val user = newUser()
        userRepository.addUser(user)

        val returned = userRepository.getUser()

        assertThat(returned).isEqualTo(user)
    }

    @Test
    fun `get user with multiple users throws`() {
        val users = (0..1)
            .map { newUser() }
        dataSourceFactory.sql2o().use {
            users.forEach { user ->
                """
                INSERT INTO users (id, displayName, refreshToken, redirectUri)
                VALUES (:id, :displayName, :refreshToken, :redirectUri)
            """.trimIndent()
                    .let(it::createQuery)
                    .bind(user)
                    .executeUpdate()
            }
        }

        assertThatThrownBy { userRepository.getUser() }.isInstanceOf(NotImplementedError::class.java)
    }

    @Test
    fun `get next link for new user is null`() {
        val user = newUser().also(userRepository::addUser)

        val nextLink = userRepository.getNextLink(user)

        assertThat(nextLink).isNull()
    }

    @Test
    fun `can set next link`() {
        val user = newUser().also(userRepository::addUser)
        val nextLink = URI.create("http://localhost/${UUID.randomUUID()}")
        userRepository.setNextLink(user, nextLink)

        val retrieved = userRepository.getNextLink(user)

        assertThat(retrieved).isEqualTo(nextLink)
    }
}