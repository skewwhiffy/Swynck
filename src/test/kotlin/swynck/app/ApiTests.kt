package swynck.app

import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.junit.Before
import org.junit.Test
import swynck.db.DataSourceFactory
import swynck.db.Migrations
import swynck.db.UserRepository
import swynck.test.utils.TestConfig

class ApiTests {
    private lateinit var dataSourceFactory: DataSourceFactory
    private lateinit var api: RoutingHttpHandler

    @Before
    fun init() {
        dataSourceFactory = DataSourceFactory(TestConfig())
        Migrations(dataSourceFactory).run()
        api = Api(UserRepository(dataSourceFactory))
    }

    @Test
    fun `current user endpoint returns null when not logged in`() {
        val response = api(Request(GET, "/user/me"))

        assertThat(response.status).isEqualTo(NOT_FOUND)
    }

    @Test
    fun `current user endpoint returns current user name`() {
        dataSourceFactory.dataSource().connection!!.use {
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