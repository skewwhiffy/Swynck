package swynck.common

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS
import com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_INTEGER_FOR_INTS
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.format.ConfigurableJackson
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

object Json : ConfigurableJackson(ObjectMapper()
        .registerModule(KotlinModule()
                .custom(ISO_OFFSET_DATE_TIME::format) { ZonedDateTime.parse(it, ISO_OFFSET_DATE_TIME) }
        )
        .disableDefaultTyping()
        .setSerializationInclusion(NON_NULL)
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(FAIL_ON_IGNORED_PROPERTIES, false)
        .configure(USE_BIG_DECIMAL_FOR_FLOATS, true)
        .configure(USE_BIG_INTEGER_FOR_INTS, true)
)

inline fun <reified T> KotlinModule.custom(crossinline write: (T) -> String, crossinline read: (String) -> T) =
        apply {
            addDeserializer(T::class.java, object : JsonDeserializer<T>() {
                override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T = read(p.text)
            })
            addSerializer(T::class.java, object : JsonSerializer<T>() {
                override fun serialize(value: T?, gen: JsonGenerator, serializers: SerializerProvider) = gen.writeString(write(value!!))
            })
        }

