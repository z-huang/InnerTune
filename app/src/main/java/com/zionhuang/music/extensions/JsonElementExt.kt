package com.zionhuang.music.extensions

import com.google.gson.*

fun Any.toJsonElement(): JsonElement = when (this) {
    is JsonElement -> this
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Char -> JsonPrimitive(this)
    else -> throw IllegalArgumentException("${this.javaClass.name} cannot be converted to JSON")
}

/**
 * JsonObject
 */
fun jsonObjectOf(vararg pairs: Pair<String, *>): JsonObject {
    val obj = JsonObject()
    for ((key, value) in pairs) {
        if (value == null) continue
        when (value) {
            is JsonElement -> obj.add(key, value)
            is Boolean -> obj.addProperty(key, value)
            is Number -> obj.addProperty(key, value)
            is String -> obj.addProperty(key, value)
            is Char -> obj.addProperty(key, value)
            else -> throw IllegalArgumentException("${value.javaClass.name} cannot be converted to JSON")
        }
    }
    return obj
}

operator fun JsonElement?.get(key: String): JsonElement? = if (this is JsonObject) this.get(key) else null
operator fun JsonObject?.get(key: String): JsonElement? = this?.get(key)
operator fun JsonObject?.set(key: String, value: JsonElement) = this?.add(key, value)
operator fun JsonObject?.contains(key: String) = this?.has(key) ?: false
fun Iterable<Pair<String, Any>>.toJsonObject(): JsonObject = JsonObject().apply {
    for (pair in this@toJsonObject) {
        this[pair.first] = pair.second.toJsonElement()
    }
}

/**
 * JsonArray
 */
fun jsonArrayOf(vararg items: Any) = JsonArray().apply {
    for (item in items) {
        add(item.toJsonElement())
    }
}

fun Array<out Any>.toJsonArray(): JsonArray = JsonArray().apply {
    for (item in this@toJsonArray) {
        add(item.toJsonElement())
    }
}

fun Iterable<Any>.toJsonArray(): JsonArray = JsonArray().apply {
    for (item in this@toJsonArray) {
        add(item.toJsonElement())
    }
}

fun CharSequence.splitToJsonArray() = JsonArray().apply {
    for (c in this@splitToJsonArray) {
        add(c)
    }
}

fun <T> Iterable<T>.mapToJsonArray(transform: (T) -> JsonElement): JsonArray = JsonArray().apply {
    for (item in this@mapToJsonArray) {
        add(transform(item))
    }
}

operator fun JsonElement?.get(index: Int): JsonElement? = if (this is JsonArray) this.get(index) else null
operator fun JsonArray?.get(index: Int): JsonElement? = this?.get(index)
fun JsonArray.isNotEmpty() = this.size() > 0
fun JsonArray.selfReverse() = this.apply {
    for (i in 0 until (size() shr 1)) {
        val temp = this[i]
        this[i] = this.get(size() - i - 1)
        this[size() - i - 1] = temp
    }
}

/**
 * JsonElement Type Extensions
 */
fun JsonElement.isString(): Boolean = this is JsonPrimitive && this.isString
fun JsonElement.isNumber(): Boolean = this is JsonPrimitive && this.isNumber

val JsonElement?.asJsonArrayOrNull: JsonArray?
    get() = if (this is JsonArray) this else null

val JsonElement?.asJsonObjectOrNull: JsonObject?
    get() = if (this is JsonObject) this else null

val JsonElement?.asBooleanOrNull: Boolean?
    get() = try {
        this?.asBoolean
    } catch (e: Exception) {
        null
    }

val JsonElement?.asStringOrNull: String?
    get() = try {
        this?.asString
    } catch (e: Exception) {
        null
    }
val JsonElement?.asStringOrBlank: String
    get() = this?.asStringOrNull ?: ""

val JsonElement?.asNumberOrNull: Number?
    get() = try {
        this?.asNumber
    } catch (e: Exception) {
        null
    }
val JsonElement?.asIntOrNull: Int?
    get() = this.asNumberOrNull?.toInt()
val JsonElement?.asFloatOrNull: Float?
    get() = this.asNumberOrNull?.toFloat()

fun Int.toJsonPrimitive(): JsonPrimitive = JsonPrimitive(this)
fun String.toJsonPrimitive(): JsonPrimitive = JsonPrimitive(this)

/**
 * JsonElement Shortcuts for "equals" methods
 */
fun JsonElement?.equals(value: Boolean) = this?.equals(JsonPrimitive(value)) ?: false
fun JsonElement?.equals(value: Number) = this?.equals(JsonPrimitive(value)) ?: false
fun JsonElement?.equals(value: String) = this?.equals(JsonPrimitive(value)) ?: false
fun JsonElement?.equals(value: Char) = this?.equals(JsonPrimitive(value)) ?: false

infix fun JsonElement.or(rhs: JsonElement): JsonElement =
        when {
            this.isNumber() && rhs.isNumber() -> (this.asInt or rhs.asInt).toJsonPrimitive()
            else -> JsonNull.INSTANCE
        }

infix fun JsonElement.xor(rhs: JsonElement): JsonElement =
        when {
            this.isNumber() && rhs.isNumber() -> (this.asInt xor rhs.asInt).toJsonPrimitive()
            else -> JsonNull.INSTANCE
        }

infix fun JsonElement.and(rhs: JsonElement): JsonElement =
        when {
            this.isNumber() && rhs.isNumber() -> (this.asInt and rhs.asInt).toJsonPrimitive()
            else -> JsonNull.INSTANCE
        }

infix fun JsonElement.shr(rhs: JsonElement): JsonElement =
        when {
            this.isNumber() && rhs.isNumber() -> (this.asInt shr rhs.asInt).toJsonPrimitive()
            else -> JsonNull.INSTANCE
        }

infix fun JsonElement.shl(rhs: JsonElement): JsonElement =
        when {
            this.isNumber() && rhs.isNumber() -> (this.asInt shl rhs.asInt).toJsonPrimitive()
            else -> JsonNull.INSTANCE
        }

operator fun JsonElement.minus(rhs: JsonElement): JsonElement =
        when {
            this.isNumber() && rhs.isNumber() -> (this.asInt - rhs.asInt).toJsonPrimitive()
            else -> JsonNull.INSTANCE
        }

operator fun JsonElement.plus(rhs: JsonElement): JsonElement =
        when {
            this.isNumber() && rhs.isNumber() -> (this.asInt + rhs.asInt).toJsonPrimitive()
            this.isString() && rhs.isString() -> (this.asString + rhs.asString).toJsonPrimitive()
            else -> JsonNull.INSTANCE
        }

operator fun JsonElement.rem(rhs: JsonElement): JsonElement =
        when {
            this.isNumber() && rhs.isNumber() -> (this.asInt % rhs.asInt).toJsonPrimitive()
            else -> JsonNull.INSTANCE
        }

operator fun JsonElement.div(rhs: JsonElement): JsonElement =
        when {
            this.isNumber() && rhs.isNumber() -> (this.asInt / rhs.asInt).toJsonPrimitive()
            else -> JsonNull.INSTANCE
        }

operator fun JsonElement.times(rhs: JsonElement): JsonElement =
        when {
            this.isNumber() && rhs.isNumber() -> (this.asInt * rhs.asInt).toJsonPrimitive()
            else -> JsonNull.INSTANCE
        }