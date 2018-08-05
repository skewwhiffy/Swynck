package swynck.db

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
        server.dataSourceFactory.dataSource().getConnection()!!.use {
            val resultSet = it.createStatement().executeQuery("show tables")
            println(resultSet)
        }
    }
}