package swynck.test.utils

import assertk.Assert
import assertk.assertions.*

fun <T> Assert<T>.satisfies(condition: (T) -> Boolean) = actual
    .let(condition)
    .let { assert(it) }
    .isEqualTo(true)

fun Assert<String>.isNotBlank() = satisfies { it.isNotBlank() }
