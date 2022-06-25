package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class Runs(
    val runs: List<Run> = emptyList(),
) {
    override fun toString() = runs.joinToString(separator = "") { it.text }
}

@Serializable
data class Run(
    val text: String,
    val navigationEndpoint: NavigationEndpoint?,
)

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

fun List<Run>.removeSeparator(): List<Run> {
    val res = mutableListOf<Run>()
    for (i in 0..lastIndex step 2) {
        res.add(get(i))
    }
    return res
}