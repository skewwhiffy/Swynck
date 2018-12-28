package swynck.util

import org.sql2o.Query

inline fun <reified T> Query.executeAndFetch() = executeAndFetch(T::class.java)
inline fun <reified T> Query.executeAndFetchFirst() = executeAndFetchFirst(T::class.java)!!
