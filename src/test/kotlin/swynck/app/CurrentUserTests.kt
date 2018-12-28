package swynck.app

import assertk.assert
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.junit.Test
import swynck.app.api.Api
import swynck.app.api.UserFound
import swynck.app.api.UserNotFound
import swynck.db.Migrations
import swynck.test.utils.TestConfig

class CurrentUserTests {
    private val config = TestConfig()
    private val dependencies = Dependencies(config)
    private val api = Api(dependencies)

    init {
        Migrations(dependencies).run()
    }

    @Test
    fun `current user endpoint returns redirect object when not logged in`() {
        config.port = 38080
        val response = api(Request(GET, "/user/me"))

        assert(response.status).isEqualTo(OK)
        val notFoundResponse = UserNotFound.lens(response)
        assert(notFoundResponse.redirect.toString()).contains("live.com")
    }

    @Test
    fun `current user endpoint returns current user name`() {
        dependencies.dataSourceFactory.dataSource().connection!!.use {
            it.createStatement().execute("""
                insert into users (id, displayName, refreshToken)
                values ('the_id', 'the_display_name', 'the_token')
            """.trimIndent())
        }

        val response = api(Request(GET, "/user/me"))

        assert(response.status).isEqualTo(OK)
        val user = UserFound.lens(response)
        assert(user.displayName).isEqualTo("the_display_name")
    }
}

