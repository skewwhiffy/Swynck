package swynck.fake.onedrive.testdata

import swynck.common.Json
import swynck.common.model.User
import java.io.File

class FakeOnedriveTestData {
    val rootFolder = getTestDataFolder()
    private val userDataFolder = File(rootFolder, "onedrive/user")
    private val userFile = File(userDataFolder, "user.json")

    fun ensureExists() {
        rootFolder.mkdirs()
        if (!rootFolder.exists()) throw Exception("Test data root folder does not exist and I could not create it at ${rootFolder.absolutePath}")
        userDataFolder.mkdirs()
        if (!userDataFolder.exists()) throw Exception("User data folder does not exist and I could not create it at ${userDataFolder.absolutePath}")
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