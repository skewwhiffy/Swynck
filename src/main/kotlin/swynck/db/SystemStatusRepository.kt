package swynck.db

import org.sql2o.Query
import swynck.util.executeAndFetch
import kotlin.collections.MutableMap.MutableEntry

class SystemStatusRepository(private val dataSourceFactory: DataSourceFactory)
    : MutableMap<String, String> {

    private fun Query.addKey(key: String) = addParameter("key", key)
    private fun Query.addValue(value: String) = addParameter("value", value)

    override val size: Int
        get() = dataSourceFactory
            .sql2o()
            .use {
                "SELECT COUNT(*) FROM SystemStatus"
                    .let(it::createQuery)
                    .executeScalar(Int::class.java)
            }

    override fun containsKey(key: String) = dataSourceFactory
        .sql2o()
        .use {
            "SELECT COUNT(*) FROM SystemStatus WHERE key = :key"
                .let(it::createQuery)
                .addKey(key)
                .executeScalar(Int::class.java)
        } > 0

    override fun containsValue(value: String) = dataSourceFactory
        .sql2o()
        .use {
            "SELECT COUNT(*) FROM SystemStatus WHERE value = :value"
                .let(it::createQuery)
                .addValue(value)
                .executeScalar(Int::class.java)
        } > 0

    override fun get(key: String) = dataSourceFactory
        .sql2o()
        .use {
            "SELECT value FROM SystemStatus WHERE key = :key"
                .let(it::createQuery)
                .addKey(key)
                .executeScalar(String::class.java)
        }

    override fun isEmpty() = size == 0

    override val entries: MutableSet<MutableEntry<String, String>>
        get() = dataSourceFactory
            .sql2o()
            .use {
                "SELECT * FROM SystemStatus"
                    .let(it::createQuery)
                    .executeAndFetch<SystemStatus>()
                    .map { MutableEntry(it.key, it.value) }
                    .toMutableSet()
            }
    override val keys: MutableSet<String>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val values: MutableCollection<String>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun clear() {
        dataSourceFactory
            .sql2o()
            .use {
                "DELETE FROM SystemStatus"
                    .let(it::createQuery)
                    .executeUpdate()
            }
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
            .use {
                val returnValue = "SELECT value FROM systemStatus WHERE KEY = :key"
                    .let(it::createQuery)
                    .addKey(key)
                    .executeScalar(String::class.java)

                "DELETE FROM systemStatus WHERE KEY = :key"
                    .let(it::createQuery)
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