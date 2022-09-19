package com.zionhuang.music.extensions

import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException

@Throws(JsonSyntaxException::class)
fun String.parseJsonString(): JsonElement = JsonParser.parseString(this)

/**
 * Database Extensions
 */
fun String.toSQLiteQuery(): SimpleSQLiteQuery = SimpleSQLiteQuery(this)
