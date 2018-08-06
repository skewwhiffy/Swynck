package swynck.db

import org.h2.jdbcx.JdbcDataSource
import org.sql2o.Sql2o
import swynck.config.Config

open class DataSourceFactory(private val config: Config) {
    private val sql2o by lazy { Sql2o(dataSource()) }
    open fun dataSource() = JdbcDataSource()
        .apply { setUrl(config.db()) }

    open fun sql2o() = sql2o.open()
}