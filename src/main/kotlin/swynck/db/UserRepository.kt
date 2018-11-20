package swynck.db

import swynck.model.User
import java.net.URI

class UserRepository(private val dataSourceFactory: DataSourceFactory) {
    fun getUser() : User? {
        val users = dataSourceFactory.sql2o().use {
            "SELECT * FROM users"
                .let(it::createQuery)
                .executeAndFetch(User::class.java)
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
                "INSERT INTO userSyncStatus (userId, nextLink) VALUES (:userId, :nextLink)"
                    .let(it::createQuery)
                    .addParameter("userId", user.id)
                    .addParameter("nextLink", nextLink.toString())
                    .executeUpdate()
            }
    }
}