package swynck.app

import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.junit.Before
import org.junit.Test
import swynck.db.Migrations
import swynck.test.utils.TestConfig

class ApiTests {
    private lateinit var dependencies: Dependencies
    private lateinit var api: RoutingHttpHandler

    @Before
    fun init() {
        dependencies = Dependencies(TestConfig())
        Migrations(dependencies.dataSourceFactory).run()
        api = Api(dependencies.userRepository, dependencies.oneDrive)
    }

    @Test
    fun `current user endpoint returns redirect object when not logged in`() {
        val response = api(Request(GET, "/user/me"))

        assertThat(response.status).isEqualTo(OK)
        val notFoundResponse = UserNotFound.lens(response)
        // TODO: Assert redirect URL is correct
    }

    @Test
    fun `current user endpoint returns current user name`() {
        dependencies.dataSourceFactory.dataSource().connection!!.use {
            it.createStatement().execute("""
                insert into public.users (name, refreshToken) values ('the_name', 'the_token')
            """.trimIndent())
        }

        val response = api(Request(GET, "/user/me"))

        assertThat(response.status).isEqualTo(OK)
        val user = User.lens(response)
        assertThat(user.name).isEqualTo("the_name")
    }
}