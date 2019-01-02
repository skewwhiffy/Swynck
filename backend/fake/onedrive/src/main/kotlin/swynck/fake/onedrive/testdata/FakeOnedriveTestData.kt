package swynck.fake.onedrive.testdata

import swynck.common.Json
import swynck.common.extensions.queryMap
import swynck.common.model.User
import swynck.real.onedrive.dto.DeltaResponse
import java.io.File
import java.net.URI

class FakeOnedriveTestData {
    val rootFolder = getTestDataFolder()
    private val userDataFolder = File(rootFolder, "onedrive/user")
    private val deltaDataFolder = File(rootFolder, "onedrive/deltas")
    private val userFile = File(userDataFolder, "user.json")
    val deltaLink: URI? by lazy {
        deltaDataFolder
            .also { if (!it.exists()) throw Exception("Delta data folder does not exist")  }
            .listFiles()
            .asSequence()
            .map { it.readText() }
            .map { Json.asA(it, DeltaResponse::class) }
            .firstOrNull { it.deltaLink != null }
            ?.deltaLink
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
        val file = File(deltaDataFolder, "${nextLink.getToken()}.json")
        return if (file.exists() && file.isFile) Json.asA(file.readText(), DeltaResponse::class)
        else null
    }

    fun putDelta(nextLink: URI?, responseRaw: String) {
        val file = File(deltaDataFolder, "${nextLink.getToken()}.json")
        if (file.exists()) throw Exception("File already exists")
        file.writeText(responseRaw)
    }

    var user: User?
        get() = if (!userFile.exists()) null
        else userFile.readText().let { Json.asA(it, User::class) }
        set(value) {
            if (value == null) userFile.delete()
            else userFile.writeText(Json.asJsonString(value))
        }

    private fun URI?.getToken() = when (this) {
        null -> "NULL"
        else -> queryMap()["token"] ?: "NULL"
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