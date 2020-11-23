package com.zionhuang.music.extractor

import com.google.gson.JsonArray
import com.google.gson.JsonElement

class JsFunctionKt(
        val block: (args: JsonArray) -> JsonElement
) : JsonElement() {
    operator fun invoke(args: JsonArray): JsonElement = block(args)
    override fun deepCopy(): JsonElement = this
    override fun toString(): String = "[JavaScript Function]"
}