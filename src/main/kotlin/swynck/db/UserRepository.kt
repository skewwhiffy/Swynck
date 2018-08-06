package swynck.db

class UserRepository(private val dataSourceFactory: DataSourceFactory) {
    fun getUser() : User? {
        val users = dataSourceFactory.sql2o().use {
            it.createQuery("SELECT * FROM users").executeAndFetch(User::class.java)
        }
        if (users.size > 1) throw NotImplementedError("Multiple logged in users")
        return users.singleOrNull()
    }
}

data class User(
    val name: String,
    val refreshToken: String
)