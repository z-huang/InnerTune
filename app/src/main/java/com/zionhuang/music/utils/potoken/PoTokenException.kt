package com.zionhuang.music.utils.potoken

/** Thrown for any recoverable failure obtaining a PoToken (transient JS/network errors). */
class PoTokenException(message: String) : Exception(message)

/** Thrown when the WebView implementation itself appears broken (fails even before init completes). */
class BadWebViewException(message: String) : Exception(message)

fun buildExceptionForJsError(error: String): Exception =
    if (error.contains("SyntaxError")) BadWebViewException(error) else PoTokenException(error)
