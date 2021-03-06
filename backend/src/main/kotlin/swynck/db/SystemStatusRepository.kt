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

    override fun get(key: String): String? = dataSourceFactory
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
                    .toMutableSet()
            }

    override val keys: MutableSet<String>
        get() = entries.map { it.key }.toMutableSet()

    override val values: MutableCollection<String>
        get() = entries.map { it.value }.toMutableList()

    override fun clear() {
        dataSourceFactory
            .sql2o()
            .use {
                "DELETE FROM SystemStatus"
                    .let(it::createQuery)
                    .executeUpdate()
            }
    }

    override fun put(key: String, value: String): String? = dataSourceFactory
        .sql2o()
        .use {
            val pair = SystemStatus(key, value)
            val returnValue = "SELECT value FROM systemStatus WHERE KEY = :key"
                .let(it::createQuery)
                .bind(pair)
                .executeScalar(String::class.java)

            "MERGE INTO systemStatus (KEY, VALUE) KEY(KEY) VALUES (:key, :value)"
                .let(it::createQuery)
                .bind(pair)
                .executeUpdate()

            returnValue
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
        override val key: String,
        override var value: String
    ) : MutableEntry<String, String> {
        override fun setValue(newValue: String): String {
            val returnValue = value
            value = newValue
            return returnValue
        }
    }
}