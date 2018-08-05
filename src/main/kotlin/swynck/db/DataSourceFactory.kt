package swynck.db

import org.h2.jdbcx.JdbcDataSource
import swynck.config.Config

open class DataSourceFactory(private val config: Config) {
    open fun dataSource() = JdbcDataSource()
        .apply { setUrl(config.db()) }
}