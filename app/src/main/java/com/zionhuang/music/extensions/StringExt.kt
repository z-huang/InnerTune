package com.zionhuang.music.extensions

import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import java.net.InetSocketAddress
import java.net.InetSocketAddress.createUnresolved

@Throws(JsonSyntaxException::class)
fun String.parseJsonString(): JsonElement = JsonParser.parseString(this)

/**
 * Database Extensions
 */
fun String.toSQLiteQuery(): SimpleSQLiteQuery = SimpleSQLiteQuery(this)

fun String.toInetSocketAddress(): InetSocketAddress {
    val (host, port) = split(":")
    return createUnresolved(host, port.toInt())
}