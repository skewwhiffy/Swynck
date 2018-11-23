package swynck.db

import org.assertj.core.api.Assertions.assertThat
import org.http4k.format.Jackson
import org.junit.Before
import org.junit.Test
import swynck.dto.onedrive.DeltaResponse
import swynck.model.User
import swynck.test.utils.TestConfig

class OnedriveMetadataRepositoryTests {
    private lateinit var dataSourceFactory: DataSourceFactory
    private lateinit var metadataRepository: OnedriveMetadataRepository
    private lateinit var userRepository: UserRepository
    private lateinit var user: User

    @Before
    fun init() {
        val config = TestConfig()
        dataSourceFactory = DataSourceFactory(config)
        Migrations(dataSourceFactory).run()
        metadataRepository = OnedriveMetadataRepository(dataSourceFactory)
        userRepository = UserRepository(dataSourceFactory)
        user = UserRepositoryTests.newUser()
        userRepository.addUser(user)
    }

    @Test
    fun `can insert root folder`() {
        val delta = """
{
    "value": [{
        "id": "${user.id.toUpperCase()}!127",
        "name": "root",
        "file": null,
        "folder": {
          "childCount": 0
        },
        "parentReference": {
          "driveId": "${user.id}",
          "id": "${user.id.toUpperCase()}!0"
        }
    }]
}
        """.trimIndent()
            .let { Jackson.asA(it, DeltaResponse::class) }

        metadataRepository.insert(delta)

        val folder = metadataRepository.getFolders(user).single()
        assertThat(folder).isEqualTo(Folder(delta.value.single().id.split("!")[1].toInt(), "root"))
    }
}