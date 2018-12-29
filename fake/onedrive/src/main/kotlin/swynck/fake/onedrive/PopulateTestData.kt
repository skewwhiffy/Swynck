package swynck.fake.onedrive

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS
import com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_INTEGER_FOR_INTS
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.format.ConfigurableJackson
import swynck.fake.onedrive.Json.auto
import java.io.File
import java.net.URI
import java.net.URLEncoder
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*

fun main(args: Array<String>) = PopulateTestData()

object PopulateTestData {
    operator fun invoke() {
        val testDataFolder = File("testData").also { it.mkdirs() }
        if (!testDataFolder.exists()) throw Exception("Test data root folder does not exist")
        val userData = UserData(testDataFolder)
        userData.getUser()
        val deltaData = DeltaData(userData, testDataFolder)
        deltaData.populate()
    }
}

class UserData(testDataFolder: File) {
    private val userData = File(testDataFolder, "onedrive/user")
    private val userFile = File(userData, "user.json")

    fun getUser(): User {
        userData.mkdirs()

        if (!userData.exists()) throw Exception("User data folder does not exist")
        if (userFile.isFile) return userFile.readText().let { Json.asA(it, User::class) }

        val authCode = getAuthCode()
        val accessToken = getAccessToken(authCode)
        val user = getUser(accessToken)
        userFile.writeText(Json.asJsonString(user))
        return user
    }

    private fun getUser(accessToken: AccessToken): User {
        val client = OkHttp()
        return Request(Method.GET, "https://graph.microsoft.com/v1.0/me/drive")
            .header("Authorization", "bearer ${accessToken.access_token}")
            .let { client(it) }
            .let { DriveResource(it) }
            .let { it.owner.user }
            .let { User(
                it.id,
                it.displayName,
                OnedriveDetails.redirectUrl,
                accessToken.refresh_token
            ) }
    }

    // TODO: Commonize this with Onedrive.kt
    private fun getAccessToken(authCode: String): AccessToken {
        val request = mapOf(
            "client_id" to OnedriveDetails.clientId,
            "redirect_uri" to OnedriveDetails.redirectUrl,
            "grant_type" to "authorization_code",
            "code" to authCode
        )
            .mapValues { v -> v.value.let { URLEncoder.encode(it, "UTF-8") } }
            .map { "${it.key}=${it.value}" }
            .joinToString("&")
            .let { Request(Method.POST, "https://login.live.com/oauth20_token.srf").body(it) }
            .header("Content-Type", "application/x-www-form-urlencoded")
        val client = OkHttp()
        val response = client(request)
        return AccessToken(response)
    }

    private fun getAuthCode(): String {
        // TODO: Think about commonizing this with Onedrive.kt in backend
        val authUrl = mapOf(
            "client_id" to OnedriveDetails.clientId,
            "scope" to OnedriveDetails.scopes.joinToString(" "),
            "redirect_uri" to OnedriveDetails.redirectUrl,
            "response_type" to "code"
        )
            .mapValues { v -> v.value.let { URLEncoder.encode(it, "UTF-8") } }
            .map { "${it.key}=${it.value}" }
            .joinToString("&")
            .let { URI("https://login.live.com/oauth20_authorize.srf?$it") }
        println("Please visit $authUrl and paste response URL back here")
        val callbackUrl = readLine()!!
            .trim()
            .let(::URI)
        return callbackUrl
            .query
            .split("&")
            .map { it.split("=") }
            .filter { it.size == 2 }
            .single { it[0] == "code" }[1]
    }
}

class DeltaData(private val userData: UserData, private val testDataFolder: File) {
    fun populate() {
        val deltaFolder = File(testDataFolder, "onedrive/deltas").also { it.mkdir() }
        if (!deltaFolder.isDirectory) throw Exception("Deltas folder does not exist")
        var nextLink: URI? = null
        var files = 0
        var folders = 0
        val nextLinkUrlMap = deltaFolder
            .listFiles()
            .map { it.readText() }
            .map { Json.asA(it, DeltaRequestAndResponse::class) }
            .map { it.requestUrl to it.response }
            .toMap()

        val user = userData.getUser()
        val accessToken = getAccessToken(user)
        var accessTokenLastRefresh = Instant.now()

        while (true) {
            if (accessTokenLastRefresh < Instant.now().minus(Duration.ofMinutes(5))) {
                println("Getting new access token")
                getAccessToken(user)
            }
            val deltaString = nextLinkUrlMap.get(nextLink) ?: getDelta(accessToken, nextLink)
            val file = File(deltaFolder, "${UUID.randomUUID()}.json")
            val payload = DeltaRequestAndResponse(
                nextLink,
                deltaString
            )
            val delta = Json.asA(deltaString, DeltaResponse::class)
            files += delta.value.count { it.file != null }
            folders += delta.value.count { it.folder != null }
            println("$files files and $folders folders so far")
            if (nextLink == delta.nextLink) {
                println("Next link has not changed")
                break
            }
            if (delta.nextLink == null) {
                println("Next link is null")
                println("Delta link is ${delta.deltaLink}")
                break
            }
            if (!nextLinkUrlMap.contains(nextLink)) file.writeText(Json.asJsonString(payload))
            nextLink = delta.nextLink
        }
    }

    // TODO: Commonize with Onedrive.kt
    private fun getDelta(accessToken: AccessToken, nextLink: URI? = null): String {
        val client = OkHttp()
        nextLink ?: return getDelta(
            accessToken,
            URI("https://graph.microsoft.com/v1.0/me/drive/root/delta")
        )
        return Request(Method.GET, nextLink.toString())
            .header("Authorization", "bearer ${accessToken.access_token}")
            .let { client(it) }
            .bodyString()
    }

    private fun getAccessToken(user: User): AccessToken {
        val request = mapOf(
            "client_id" to OnedriveDetails.clientId,
            "redirect_uri" to user.redirectUri,
            "grant_type" to "refresh_token",
            "refresh_token" to user.refreshToken
        )
            .mapValues { v -> v.value.let { URLEncoder.encode(it, "UTF-8") } }
            .map { "${it.key}=${it.value}" }
            .joinToString("&")
            .let { Request(Method.POST, "https://login.live.com/oauth20_token.srf").body(it) }
            .header("Content-Type", "application/x-www-form-urlencoded")
        val client = OkHttp()
        val response = client(request)
        return if (response.status.successful) AccessToken(response)
        else throw IllegalArgumentException("Problem getting access token: ${response.bodyString()}")
    }
}

@Suppress("SpellCheckingInspection")
object OnedriveDetails {
    const val redirectUrl = "https://login.live.com/oauth20_desktop.srf"
    const val clientId = "21133f26-e5d8-486b-8b27-0801db6496a9"
    const val clientSecret = "gcyhkJZK73!$:zqHNBE243}"
    val scopes = setOf("files.readwrite", "offline_access")
}

// TODO: Commonize with Onedrive.kt
data class DriveResource(
    val id: String,
    val owner: IdentitySetResource
) {
    companion object {
        private val lens = Body.auto<DriveResource>().toLens()
        operator fun invoke(response: Response) = lens(response)
        data class IdentitySetResource(
            val user: IdentityResource
        ) {
            companion object {
                data class IdentityResource(
                    val displayName: String,
                    val id: String
                )
            }
        }
    }
}
data class AccessToken(
    val refresh_token: String,
    val access_token: String,
    val expires_in: Int
) {
    companion object {
        private val lens = Body.auto<AccessToken>().toLens()
        operator fun invoke(response: Response) = lens(response)
    }
}

// TODO: Commonize with User.kt
data class User(
    val id: String,
    val displayName: String,
    val redirectUri: String,
    val refreshToken: String
)

// Commonize with Json.kt
@Suppress("unused")
object Json : ConfigurableJackson(ObjectMapper()
    .registerModule(KotlinModule()
        .custom(ISO_OFFSET_DATE_TIME::format) { ZonedDateTime.parse(it, ISO_OFFSET_DATE_TIME) }
    )
    .disableDefaultTyping()
    .setSerializationInclusion(NON_NULL)
    .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(FAIL_ON_IGNORED_PROPERTIES, false)
    .configure(USE_BIG_DECIMAL_FOR_FLOATS, true)
    .configure(USE_BIG_INTEGER_FOR_INTS, true)
)

inline fun <reified T> KotlinModule.custom(crossinline write: (T) -> String, crossinline read: (String) -> T) =
    apply {
        addDeserializer(T::class.java, object : JsonDeserializer<T>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T = read(p.text)
        })
        addSerializer(T::class.java, object : JsonSerializer<T>() {
            override fun serialize(value: T?, gen: JsonGenerator, serializers: SerializerProvider) = gen.writeString(write(value!!))
        })
    }

data class DeltaRequestAndResponse(
    val requestUrl: URI?,
    val response: String
)

// TODO: Commonize with Resources.kt
data class DeltaResponse(
    @JsonProperty("@odata.nextLink")
    val nextLink: URI?,
    @JsonProperty("@odata.deltaLink")
    val deltaLink: URI?,
    val value: List<DriveItem>
) {
    companion object {
        private val lens = Body.auto<DeltaResponse>().toLens()
        operator fun invoke(response: Response) = lens(response)
    }
}

data class DriveItem(
    val id: String,
    val name: String,
    val file: FileItem?,
    val folder: FolderItem?,
    val `package`: PackageItem?,
    val parentReference: ParentReference
)

data class FileItem(
    val mimeType: String
)

data class FolderItem(
    val childCount: Int
)

data class PackageItem(
    val type: String
)

data class ParentReference(
    val driveId: String,
    val id: String
)
