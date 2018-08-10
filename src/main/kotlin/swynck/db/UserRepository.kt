package swynck.db

import swynck.model.User
import swynck.service.UserDetails

class UserRepository(private val dataSourceFactory: DataSourceFactory) {
    fun getUser() : User? {
        val users = dataSourceFactory.sql2o().use {
            it.createQuery("SELECT * FROM users").executeAndFetch(User::class.java)
        }
        if (users.size > 1) throw NotImplementedError("Multiple logged in users")
        return users.singleOrNull()
    }

    fun addUser(
        userDetails: UserDetails,
        refreshToken: String
    ) {
        if (getUser() != null) throw NotImplementedError("Multiple users")
        dataSourceFactory.sql2o().use {
            it.createQuery("""
                INSERT INTO users (id, displayName, refreshToken)
                VALUES (:id, :displayName, :refreshToken)
            """.trimIndent())
                .addParameter("id", userDetails.id)
                .addParameter("displayName", userDetails.displayName)
                .addParameter("refreshToken", refreshToken)
                .executeUpdate()
        }
    }
}