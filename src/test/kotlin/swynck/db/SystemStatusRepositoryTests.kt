package swynck.db

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import swynck.test.utils.TestConfig
import java.util.*

class SystemStatusRepositoryTests : MapTestBase({
    val config = TestConfig()
    val dataSourceFactory = DataSourceFactory(config)
    Migrations(dataSourceFactory).run()
    SystemStatusRepository(dataSourceFactory)
})

class MapTests : MapTestBase({ mutableMapOf() })

abstract class MapTestBase(private val makeMap: () -> MutableMap<String, String>) {
    lateinit var map: MutableMap<String, String>

    @Before
    fun init() {
        map = makeMap()
    }

    @Test
    fun `put element returns correct value`() {
        val originalValue = getNewString()
        val originalKey = getNewString()
        val returnValue = map.put(originalKey, originalValue)

        assertThat(returnValue).isNull()

        val nextReturnValue = map.put(originalKey, getNewString())

        assertThat(nextReturnValue).isEqualTo(originalValue)
    }

    @Test
    fun `size increases with new elements`() {
        val originalSize = map.size
        map[getNewString()] = getNewString()

        val newSize = map.size

        assertThat(newSize).isEqualTo(originalSize + 1)
    }

    @Test
    fun `remove element returns correct value`() {
        val value = getNewString()
        val key = getNewString()
        map[key] = value

        val firstReturnValue = map.remove(key)
        val secondReturnValue = map.remove(key)

        assertThat(firstReturnValue).isEqualTo(value)
        assertThat(secondReturnValue).isNull()
    }

    @Test
    fun `size decreases when removing elements`() {
        val keys = (0..5).map { "${UUID.randomUUID()}"}
        keys.forEach{ map[it] = it }
        val originalSize = map.size

        map.remove(keys[0])

        assertThat(map.size).isEqualTo(originalSize - 1)
    }

    @Test
    fun `containsKey is true only if key value is defined`() {
        val key = getNewString()
        assertThat(map.containsKey(key)).isFalse()

        map[key] = getNewString()

        assertThat(map.containsKey(key)).isTrue()
    }

    private fun getNewString() = "${UUID.randomUUID()}"
}