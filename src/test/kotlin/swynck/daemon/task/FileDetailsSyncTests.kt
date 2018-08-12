package swynck.daemon.task

import org.assertj.core.api.Assertions.assertThat
import org.h2.jdbcx.JdbcDataSource
import org.junit.BeforeClass
import org.junit.Test
import org.sql2o.Sql2o
import java.io.File

class FileDetailsSyncTests {
    companion object {
        private var refreshTokenExists = false
        private lateinit var refreshToken: String
        @BeforeClass
        @JvmStatic
        fun initClass() {
            val home = System.getProperty("user.home")
            val file = File("$home/.config/swynck.mv.db")
            if (!file.exists()) {
                println("DB does not exist at ${file.absolutePath}")
                return
            }
            val dataSource = JdbcDataSource().apply { setUrl("jdbc:h2:~/.config/swynck")}
            val sql2o = Sql2o(dataSource)
            sql2o.open().use {
                val refreshTokens = it
                    .createQuery("SELECT refreshToken FROM users")
                    .executeAndFetch(String::class.java)
                if (refreshTokens.size != 1) {
                    println("More than one refresh token")
                    return
                }
                refreshToken = refreshTokens.single()
                refreshTokenExists = true
            }
        }
    }

    @Test
    fun `folders are populated`() {
        if (!refreshTokenExists) return
        println(refreshToken)
    }
}