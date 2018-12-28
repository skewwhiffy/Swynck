package swynck.model

data class User(
    val id: String,
    val displayName: String,
    val redirectUri: String,
    val refreshToken: String
)