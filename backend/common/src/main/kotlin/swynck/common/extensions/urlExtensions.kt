package swynck.common.extensions

import java.net.URI

fun URI.queryMap() = query?.queryMap() ?: mapOf()

fun String.queryMap() = split("&")
    .map { it.split("=") }
    .filter { it.size == 2 }
    .map { it[0] to it[1] }
    .toMap()
