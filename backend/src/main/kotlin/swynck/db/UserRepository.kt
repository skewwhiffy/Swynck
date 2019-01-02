package swynck.db

import swynck.common.model.User
import swynck.util.executeAndFetch
import java.net.URI

class UserRepository(private val dataSourceFactory: DataSourceFactory) {
    fun getUser() : User? {
        val users = dataSourceFactory.sql2o().use {
            "SELECT * FROM users"
                .let(it::createQuery)
                .executeAndFetch<User>()
        }
        if (users.size > 1) throw NotImplementedError("Multiple logged in users")
        return users.singleOrNull()
    }

    fun addUser(user: User) {
        if (getUser() != null) throw NotImplementedError("Multiple users")
        dataSourceFactory.sql2o().use {
            """
INSERT INTO users (id, displayName, refreshToken, redirectUri)
VALUES (:id, :displayName, :refreshToken, :redirectUri)
            """.trimIndent()
                .let(it::createQuery)
                .bind(user)
                .executeUpdate()
        }
    }

    fun getNextLink(user: User) = dataSourceFactory
        .sql2o()
        .use {
            "SELECT nextLink FROM userSyncStatus WHERE userId = :userId"
                .let(it::createQuery)
                .addParameter("userId", user.id)
                .executeScalar(String::class.java)
                ?.let(URI::create)
        }

    fun setNextLink(user: User, nextLink: URI) {
        dataSourceFactory
            .sql2o()
            .use {
                """
MERGE INTO userSyncStatus (userId, nextLink) KEY (userId) VALUES (:userId, :nextLink)
                """.trimIndent()
                    .let(it::createQuery)
                    .addParameter("userId", user.id)
                    .addParameter("nextLink", nextLink.toString())
                    .executeUpdate()
            }
    }
}