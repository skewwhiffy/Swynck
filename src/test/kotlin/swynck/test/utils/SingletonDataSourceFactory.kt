package swynck.test.utils

import org.h2.jdbcx.JdbcDataSource
import swynck.config.Config
import swynck.db.DataSourceFactory

class SingletonDataSourceFactory(config: Config) : DataSourceFactory(config) {
    private val source = JdbcDataSource().apply { setUrl(config.db()) }

    override fun dataSource() = source
}