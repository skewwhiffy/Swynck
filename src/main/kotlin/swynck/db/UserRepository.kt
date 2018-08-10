package swynck.db

import swynck.model.User

class UserRepository(private val dataSourceFactory: DataSourceFactory) {
    fun getUser() : User? {
        val users = dataSourceFactory.sql2o().use {
            it.createQuery("SELECT * FROM users").executeAndFetch(User::class.java)
        }
        if (users.size > 1) throw NotImplementedError("Multiple logged in users")
        return users.singleOrNull()
    }

    fun addUser(user: User) {
        if (getUser() != null) throw NotImplementedError("Multiple users")
        dataSourceFactory.sql2o().use {
            it.createQuery("""
                INSERT INTO users (id, displayName, refreshToken)
                VALUES (:id, :displayName, :refreshToken)
            """.trimIndent())
                .addParameter("id", user.id)
                .addParameter("displayName", user.displayName)
                .addParameter("refreshToken", user.refreshToken)
                .executeUpdate()
        }
    }
}