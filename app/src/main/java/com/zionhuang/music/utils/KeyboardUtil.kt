package com.zionhuang.music.utils

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.getSystemService

object KeyboardUtil {
    fun showKeyboard(activity: Activity, editText: EditText) {
        if (editText.requestFocus()) {
            val imm = activity.getSystemService<InputMethodManager>()!!
            imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED)
        }
    }

    fun hideKeyboard(activity: Activity, editText: EditText) {
        val imm = activity.getSystemService<InputMethodManager>()!!
        imm.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
        editText.clearFocus()
    }
}