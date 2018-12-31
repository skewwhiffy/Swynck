package swynck.fake.onedrive

import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import swynck.common.Json
import swynck.real.onedrive.client.AccessToken
import swynck.real.onedrive.client.DriveResource
import swynck.real.onedrive.dto.DeltaResponse
import java.io.File
import java.net.URI
import java.net.URLEncoder
import java.time.Duration
import java.time.Instant
import java.util.*

fun main(args: Array<String>) = PopulateTestData()

object PopulateTestData {
    operator fun invoke() {
        val testDataFolder = getTestDataFolder()
        testDataFolder.mkdirs()
        if (!testDataFolder.exists()) throw Exception("Test data root folder does not exist")
        println("Using test data folder ${testDataFolder.absolutePath}")
        val userData = UserData(testDataFolder)
        userData.getUser()
        val deltaData = DeltaData(userData, testDataFolder)
        deltaData.populate()
    }

    private fun getTestDataFolder(): File {
        var rootDirectory = File(System.getProperty("user.dir"))
        while (!rootDirectory.listFiles().any { it.isDirectory && it.name == "gradle" }) {
            rootDirectory = rootDirectory.parentFile
            if (rootDirectory == null) throw Exception("Could not find root directory")
        }
        return File(rootDirectory, "testData")
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
        // TODO: Only get access token if needed
        val accessToken = getAccessToken(user)
        var accessTokenLastRefresh = Instant.now()

        while (true) {
            if (accessTokenLastRefresh < Instant.now().minus(Duration.ofMinutes(5))) {
                println("Getting new access token")
                getAccessToken(user)
                accessTokenLastRefresh = Instant.now()
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
        val request = Request(Method.GET, nextLink.toString())
            .header("Authorization", "bearer ${accessToken.access_token}")
        var attempts = 5
        while (true) {
            val response = client(request)
            if (response.status == OK) return response.bodyString()
            if (attempts-- < 0) throw Exception("I've tried lots, but failed. I give up")
            println("Call failed: $response. Backing off and trying again")
            Thread.sleep(5000)
        }
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

// TODO: Commonize with User.kt
data class User(
    val id: String,
    val displayName: String,
    val redirectUri: String,
    val refreshToken: String
)


data class DeltaRequestAndResponse(
    val requestUrl: URI?,
    val response: String
)
