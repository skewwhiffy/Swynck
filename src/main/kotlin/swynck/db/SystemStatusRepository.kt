package swynck.db

import org.sql2o.Query
import swynck.util.executeAndFetchFirst
import kotlin.collections.MutableMap.MutableEntry

class SystemStatusRepository(private val dataSourceFactory: DataSourceFactory)
    : MutableMap<String, String> {

    private fun Query.addKey(key: String) = addParameter("key", key)

    override val size: Int
        get() = dataSourceFactory
            .sql2o()
            .use {
                "SELECT COUNT(*) FROM SystemStatus"
                    .let(it::createQuery)
                    .executeAndFetchFirst()
            }

    override fun containsKey(key: String) = dataSourceFactory
        .sql2o()
        .use {
            "SELECT COUNT(*) FROM SystemStatus WHERE key = :key"
                .let(it::createQuery)
                .addKey(key)
                .executeScalar(Int::class.java)
        } > 0

    override fun containsValue(value: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(key: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isEmpty(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val entries: MutableSet<MutableEntry<String, String>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val keys: MutableSet<String>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val values: MutableCollection<String>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun clear() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun put(key: String, value: String): String? {
        val pair = SystemStatus(key, value)
        return dataSourceFactory
            .sql2o()
            .run {
                val returnValue = "SELECT value FROM systemStatus WHERE KEY = :key"
                    .let(::createQuery)
                    .bind(pair)
                    .executeScalar(String::class.java)

                "MERGE INTO systemStatus (KEY, VALUE) KEY(KEY) VALUES (:key, :value)"
                    .let(::createQuery)
                    .bind(pair)
                    .executeUpdate()

                returnValue
            }
    }

    override fun putAll(from: Map<out String, String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(key: String): String? {
        return dataSourceFactory
            .sql2o()
            .run {
                val returnValue = "SELECT value FROM systemStatus WHERE KEY = :key"
                    .let(::createQuery)
                    .addKey(key)
                    .executeScalar(String::class.java)

                "DELETE FROM systemStatus WHERE KEY = :key"
                    .let(::createQuery)
                    .addKey(key)
                    .executeUpdate()

                returnValue
            }
    }

    data class SystemStatus(
        val key: String,
        val value: String
    )
}