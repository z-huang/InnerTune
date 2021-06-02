package com.zionhuang.music.extensions

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject

fun JsonArray.getLastObject(): JsonObject = getObject(size - 1)