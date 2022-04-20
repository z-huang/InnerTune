package com.zionhuang.music.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.requireAppCompatActivity(): AppCompatActivity = requireActivity() as AppCompatActivity