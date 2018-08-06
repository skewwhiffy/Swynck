package swynck.db

import org.assertj.core.api.Assertions.assertThat
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import swynck.test.utils.StartServerForTesting

class MigrationsTests {
    companion object {
        private lateinit var server: StartServerForTesting

        @JvmStatic
        @BeforeClass
        fun initClass() {
            server = StartServerForTesting()
        }

        @JvmStatic
        @AfterClass
        fun teardownClass() {
            server.close()
        }
    }

    @Test
    fun `user table exists`() {
        server.dataSourceFactory.sql2o().use { connection ->
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