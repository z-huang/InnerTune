@file:OptIn(ExperimentalSerializationApi::class)

package com.zionhuang.innertube.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class Continuation(
    @JsonNames("nextContinuationData", "nextRadioContinuationData")
    val nextContinuationData: NextContinuationData?,
) {
    @Serializable
    data class NextContinuationData(
        val continuation: String,
    )
}

fun List<Continuation>.getContinuation() =
    get(0).nextContinuationData?.continuation


fun List<Continuation>.getContinuations() =
    mapNotNull { it.nextContinuationData?.continuation }
        .ifEmpty { null }