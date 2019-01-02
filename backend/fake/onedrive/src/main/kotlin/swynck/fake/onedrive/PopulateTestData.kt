package swynck.fake.onedrive

import org.http4k.core.Status.Companion.OK
import swynck.common.Config
import swynck.common.Json
import swynck.common.extensions.queryMap
import swynck.common.model.User
import swynck.fake.onedrive.testdata.FakeOnedriveTestData
import swynck.real.onedrive.client.OnedriveClientsImpl
import swynck.real.onedrive.client.OnedriveWrapper
import swynck.real.onedrive.dto.AccessToken
import swynck.real.onedrive.dto.DeltaResponse
import java.net.URI
import java.time.Duration
import java.time.Instant

fun main(args: Array<String>) = PopulateTestData()

object PopulateTestData {
    private val redirectUri = URI("https://login.live.com/oauth20_desktop.srf")
    private val fakeOnedriveTestData = FakeOnedriveTestData()
    private val onedrive = OnedriveWrapper(OnedriveClientsImpl(), Config())

    operator fun invoke() {
        fakeOnedriveTestData.ensureExists()
        println("Using test data folder ${fakeOnedriveTestData.rootFolder.absolutePath}")
        val user = getUser()
        val deltaData = DeltaData(fakeOnedriveTestData, onedrive, user)
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
            .queryMap()["code"]!!
    }
}

class DeltaData(
    private val fakeOnedriveTestData: FakeOnedriveTestData,
    private val onedrive: OnedriveWrapper,
    private val user: User
) {
    fun populate() {
        var nextLink: URI? = null
        var batches = 0

        var accessToken = lazy { onedrive.getAccessToken(user) }
        var accessTokenLastRefresh = Instant.now()

        while (true) {
            if (accessTokenLastRefresh < Instant.now().minus(Duration.ofMinutes(5))) {
                println("Getting new access token")
                accessToken = lazy { onedrive.getAccessToken(user) }
                accessTokenLastRefresh = Instant.now()
            }
            var cached = true
            val deltaResponse = fakeOnedriveTestData.getDelta(nextLink)
                ?: getDelta(accessToken.value, nextLink)
                    .also { cached = false }
                    .also { fakeOnedriveTestData.putDelta(nextLink, it) }
                    .let { Json.asA(it, DeltaResponse::class) }
            batches++
            println("Populated $batches batches ${if (cached) "CACHED" else ""}")
            if (nextLink == deltaResponse.nextLink) {
                println("Next link has not changed")
                break
            }
            if (deltaResponse.nextLink == null) {
                println("Next link is null")
                println("Delta link is ${deltaResponse.deltaLink}")
                break
            }
            nextLink = deltaResponse.nextLink
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
