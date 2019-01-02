package swynck.fake.onedrive.testdata

import swynck.common.Json
import swynck.common.extensions.queryMap
import swynck.common.model.User
import swynck.fake.onedrive.DeltaRequestAndResponse
import swynck.real.onedrive.dto.DeltaResponse
import java.io.File
import java.net.URI

class FakeOnedriveTestData {
    val rootFolder = getTestDataFolder()
    private val userDataFolder = File(rootFolder, "onedrive/user")
    private val deltaDataFolder = File(rootFolder, "onedrive/deltas")
    private val userFile = File(userDataFolder, "user.json")
    private val deltas: Map<URI?, DeltaResponse> by lazy {
        deltaDataFolder
            .listFiles()
            .map { it.readText() }
            .map { Json.asA(it, DeltaRequestAndResponse::class) }
            .map { it.requestUrl to it.response.let { Json.asA(it, DeltaResponse::class) } }
            .toMap()
    }

    fun ensureExists() {
        rootFolder.mkdirs()
        if (!rootFolder.exists()) throw Exception("Test data root folder does not exist and I could not create it at ${rootFolder.absolutePath}")
        userDataFolder.mkdirs()
        if (!userDataFolder.exists()) throw Exception("User data folder does not exist and I could not create it at ${userDataFolder.absolutePath}")
        deltaDataFolder.mkdirs()
        if (!deltaDataFolder.exists()) throw Exception("Delta data folder does not exist and I could not create it at ${deltaDataFolder.absolutePath}")
    }

    fun getDelta(nextLink: URI?): DeltaResponse? {
        nextLink ?: return deltas[null]
        val query = nextLink.queryMap()
        val token = query["token"] ?: return deltas[null]
        return deltas
            .keys
            .firstOrNull { it?.queryMap()?.get("token") == token }
            ?.let { deltas[it] }
    }

    var user: User?
        get() = if (!userFile.exists()) null
            else userFile.readText().let { Json.asA(it, User::class) }
        set(value) {
            if (value == null) userFile.delete()
            else userFile.writeText(Json.asJsonString(value))
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