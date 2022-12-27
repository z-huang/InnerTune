package com.zionhuang.music.extensions

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

fun Fragment.requireAppCompatActivity(): AppCompatActivity = requireActivity() as AppCompatActivity

val Fragment.sharedPreferences get() = requireContext().sharedPreferences

fun DialogFragment.show(context: Context, tag: String? = null) {
    context.getActivity()?.let {
        show(it.supportFragmentManager, tag)
    }
}