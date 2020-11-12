package com.zionhuang.music.extensions

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

/**
 * JsonObject
 */
// convenient initialization method
fun jsonObject(vararg pairs: Pair<String, *>): JsonObject {
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

// "get" & "set" operators
operator fun JsonElement?.get(key: String): JsonElement? = if (this is JsonObject) this.get(key) else null
operator fun JsonObject?.get(key: String): JsonElement? = this?.get(key)
operator fun JsonObject?.set(key: String, value: JsonElement) = this?.add(key, value)

// "in" operator
operator fun JsonObject?.contains(key: String) = this?.has(key) ?: false

/**
 * JsonArray
 */
// "get" operator
operator fun JsonElement?.get(index: Int): JsonElement? = if (this is JsonArray) this.get(index) else null
operator fun JsonArray?.get(index: Int): JsonElement? = this?.get(index)
fun JsonArray.isNotEmpty() = this.size() > 0

/**
 * JsonElement Type Converting Extensions
 */
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

/**
 * JsonElement Shortcuts for "equals" methods
 */
fun JsonElement?.equals(value: Boolean) = this?.equals(JsonPrimitive(value)) ?: false
fun JsonElement?.equals(value: Number) = this?.equals(JsonPrimitive(value)) ?: false
fun JsonElement?.equals(value: String) = this?.equals(JsonPrimitive(value)) ?: false
fun JsonElement?.equals(value: Char) = this?.equals(JsonPrimitive(value)) ?: false

infix fun JsonElement.or(rhs: JsonElement): JsonElement {
    TODO()
}

infix fun JsonElement.xor(rhs: JsonElement): JsonElement {
    TODO()
}

infix fun JsonElement.and(rhs: JsonElement): JsonElement {
    TODO()
}

infix fun JsonElement.shr(rhs: JsonElement): JsonElement {
    TODO()
}

infix fun JsonElement.shl(rhs: JsonElement): JsonElement {
    TODO()
}

operator fun JsonElement.plus(rhs: JsonElement): JsonElement {
    TODO()
}

operator fun JsonElement.minus(rhs: JsonElement): JsonElement {
    TODO()
}

operator fun JsonElement.rem(rhs: JsonElement): JsonElement {
    TODO()
}

operator fun JsonElement.div(rhs: JsonElement): JsonElement {
    TODO()
}

operator fun JsonElement.times(rhs: JsonElement): JsonElement {
    TODO()
}