package com.zionhuang.music.utils

import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

fun reportException(throwable: Throwable) {
    throwable.printStackTrace()
    try {
        FirebaseApp.getInstance()
    } catch (e: IllegalStateException) {
        // No default FirebaseApp (e.g. a build without google-services.json, such as CI/PR
        // builds) -- Crashlytics can't be used, but the exception itself is still recoverable
        // for the caller, so don't let a missing Firebase config turn it into a crash.
        return
    }
    Firebase.crashlytics.recordException(throwable)
}
