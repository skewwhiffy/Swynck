package swynck.db

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import swynck.test.utils.StartServerForTesting

class MigrationsTests {
    private val server = StartServerForTesting()

    @After
    fun tearDown() = server.close()

    @Test
    fun `user table exists`() {
        server.dependencies.dataSourceFactory.sql2o().use { connection ->
            val tables = connection.createQuery("show tables").executeAndFetch(Table::class.java)

            val candidates = tables.filter { it.table_name.toLowerCase() == "users" }
            assertThat(candidates).isNotEmpty
        }
    }

    data class Table(
        val table_name: String,
        val table_schema: String
    )
}