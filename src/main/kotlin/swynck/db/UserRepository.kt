package swynck.db

class UserRepository(private val dataSourceFactory: DataSourceFactory) {
    fun getUser() : User? {
        val users = dataSourceFactory.sql2o().use {
            it.createQuery("SELECT * FROM users").executeAndFetch(User::class.java)
        }
        if (users.size > 1) throw NotImplementedError("Multiple logged in users")
        return users.singleOrNull()
    }

    fun addUser(email: String, refreshToken: String) {
        if (getUser() != null) throw NotImplementedError("Multiple users")
        dataSourceFactory.sql2o().use {
            it.createQuery("""
                INSERT INTO users (email, refreshToken)
                VALUES (:email, :refreshToken)
            """.trimIndent())
                .addParameter("email", email)
                .addParameter("refreshToken", refreshToken)
                .executeUpdate()
        }
    }
}

data class User(
    val email: String,
    val refreshToken: String
)