package com.zionhuang.music.extensions

import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

fun EditText.getTextChangeFlow(): StateFlow<String> {
    val query = MutableStateFlow(text.toString())
    doOnTextChanged { text, _, _, _ ->
        query.value = text.toString()
    }
    return query
}
