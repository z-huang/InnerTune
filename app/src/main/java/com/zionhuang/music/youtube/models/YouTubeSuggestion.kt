package com.zionhuang.music.youtube.models

import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.zionhuang.music.extensions.asJsonArrayOrNull
import com.zionhuang.music.extensions.asStringOrBlank
import com.zionhuang.music.extensions.get
import java.util.*

data class YouTubeSuggestion(val query: String, val suggestions: List<String>) {
    companion object {
        @JvmField
        val deserializer = JsonDeserializer { json: JsonElement, _, _ ->
            val suggestions = ArrayList<String>()
            json.asJsonArrayOrNull[1].asJsonArrayOrNull?.let {
                for (jsonElement in it) {
                    suggestions += jsonElement.asStringOrBlank
                }
            }
            YouTubeSuggestion(json[0].asStringOrBlank, suggestions)
        }
    }
}