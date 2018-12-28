package swynck.db

import assertk.assert
import assertk.assertions.isNotEmpty
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
            assert(candidates).isNotEmpty()
        }
    }

    data class Table(
        val table_name: String,
        val table_schema: String
    )
}