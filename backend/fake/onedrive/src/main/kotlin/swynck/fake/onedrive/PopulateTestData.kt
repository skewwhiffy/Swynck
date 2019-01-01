package swynck.fake.onedrive

import org.http4k.core.Status.Companion.OK
import swynck.common.Config
import swynck.common.Json
import swynck.common.model.User
import swynck.fake.onedrive.testdata.FakeOnedriveTestData
import swynck.real.onedrive.client.OnedriveClientsImpl
import swynck.real.onedrive.client.OnedriveWrapper
import swynck.real.onedrive.dto.AccessToken
import swynck.real.onedrive.dto.DeltaResponse
import java.io.File
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.*

fun main(args: Array<String>) = PopulateTestData()

object PopulateTestData {
    private val redirectUri = URI("https://login.live.com/oauth20_desktop.srf")
    private val fakeOnedriveTestData = FakeOnedriveTestData()
    private val onedrive = OnedriveWrapper(OnedriveClientsImpl(), Config())

    operator fun invoke() {
        fakeOnedriveTestData.ensureExists()
        println("Using test data folder ${fakeOnedriveTestData.rootFolder.absolutePath}")
        val user = getUser()
        val deltaData = DeltaData(onedrive, user, fakeOnedriveTestData.rootFolder)
        deltaData.populate()
    }

    private fun getUser(): User {
        fakeOnedriveTestData.user?.let { return it }
        val authCode = getAuthCode()
        val accessToken = onedrive.getAccessToken(authCode, redirectUri)
        val user = onedrive.getUser(accessToken, redirectUri)
        fakeOnedriveTestData.user = user
        return user
    }

    private fun getAuthCode(): String {
        val authUrl = onedrive.authenticationUrl(redirectUri)
        println("Please visit $authUrl and paste response URL back here")
        val callbackUrl = readLine()!!
        return callbackUrl
            .trim()
            .let(::URI)
            .query
            .split("&")
            .map { it.split("=") }
            .filter { it.size == 2 }
            .single { it[0] == "code" }[1]
    }
}

class DeltaData(
    private val onedrive: OnedriveWrapper,
    private val user: User,
    private val testDataFolder: File
) {
    fun populate() {
        val deltaFolder = File(testDataFolder, "onedrive/deltas").also { it.mkdir() }
        if (!deltaFolder.isDirectory) throw Exception("Deltas folder does not exist")
        var nextLink: URI? = null
        var batches = 0
        val files = mutableSetOf<String>()
        val folders = mutableSetOf<String>()
        val nextLinkUrlMap = deltaFolder
            .listFiles()
            .map { it.readText() }
            .map { Json.asA(it, DeltaRequestAndResponse::class) }
            .map { it.requestUrl to it.response }
            .toMap()

        var accessToken = lazy { onedrive.getAccessToken(user) }
        var accessTokenLastRefresh = Instant.now()

        while (true) {
            if (accessTokenLastRefresh < Instant.now().minus(Duration.ofMinutes(5))) {
                println("Getting new access token")
                accessToken = lazy { onedrive.getAccessToken(user) }
                accessTokenLastRefresh = Instant.now()
            }
            val deltaString = nextLinkUrlMap.get(nextLink) ?: getDelta(accessToken.value, nextLink)
            val file = File(deltaFolder, "${UUID.randomUUID()}.json")
            val payload = DeltaRequestAndResponse(
                nextLink,
                deltaString
            )
            val delta = Json.asA(deltaString, DeltaResponse::class)
            files += delta.value.filter { it.file != null }.map { it.name }
            folders += delta.value.filter { it.folder != null }.map { it.name }
            batches++
            println("Populated $batches batches ${files.size} files and ${folders.size} folders")
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

    private fun getDelta(accessToken: AccessToken, nextLink: URI? = null): String {
        var attempts = 5
        while (true) {
            val response = onedrive.getDeltaRaw(accessToken, nextLink)
            if (response.status == OK) return response.bodyString()
            if (attempts-- < 0) throw Exception("I've tried lots, but failed. I give up")
            println("Call failed: $response. Backing off and trying again")
            Thread.sleep(5000)
        }
    }
}

data class DeltaRequestAndResponse(
    val requestUrl: URI?,
    val response: String
)
