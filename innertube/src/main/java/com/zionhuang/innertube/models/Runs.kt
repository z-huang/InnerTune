package com.zionhuang.innertube.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class Runs(
    val runs: List<Run> = emptyList(),
) {
    override fun toString() = runs.joinToString(separator = "") { it.text }
}

fun List<Run>.asString() = joinToString(separator = "") { it.text }

@Parcelize
@Serializable
data class Run(
    val text: String,
    val navigationEndpoint: NavigationEndpoint?,
) : Parcelable {
    inline fun <reified T : Endpoint> toLink(): Link<T>? =
        (navigationEndpoint?.endpoint as? T)?.let {
            Link(text, it)
        }
}

fun List<Run>.splitBySeparator(): List<List<Run>> {
    val res = mutableListOf<List<Run>>()
    var tmp = mutableListOf<Run>()
    forEach { run ->
        if (run.text == " • ") {
            res.add(tmp)
            tmp = mutableListOf()
        } else {
            tmp.add(run)
        }
    }
    res.add(tmp)
    return res
}

fun List<Run>.oddElements() = filterIndexed { index: Int, _: Run ->
    index % 2 == 0
}
