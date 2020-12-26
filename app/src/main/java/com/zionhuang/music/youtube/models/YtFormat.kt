package com.zionhuang.music.youtube.models

import com.google.gson.JsonObject
import com.zionhuang.music.extensions.asFloatOrNull
import com.zionhuang.music.extensions.asIntOrNull
import com.zionhuang.music.extensions.asStringOrNull

data class YtFormat(
        val formatId: String? = null,
        val ext: String? = null,
        val width: Int? = null,
        val height: Int? = null,
        var stretchedRatio: Float? = null,
        val formatNote: String? = null,
        val acodec: String? = null,
        val vcodec: String? = null,
        val fps: Int? = null,
        val tbr: Float? = null, // bitrate of audio and video (KBit/s)
        val abr: Int? = null, // audio bitrate (KBit/s)
        val asr: Float? = null, // audio sampling rate (Hz)
        val vbr: Float? = null, // video bitrate (KBit/s)
        val url: String? = null,
        val protocol: String? = null,
        val fileSize: Int? = null,
)

fun JsonObject.toYtFormat() =
        YtFormat(
                formatId = this["format_id"].asStringOrNull,
                width = this["ext"].asIntOrNull,
                height = this["height"].asIntOrNull,
                formatNote = (this["qualityLabel"] ?: this["quality"]).asStringOrNull,
                fps = this["fps"].asIntOrNull,
                tbr = this["tbr"].asFloatOrNull,
                abr = this["abr"].asIntOrNull,
                vbr = this["vbr"].asFloatOrNull,
                asr = this["asr"].asFloatOrNull
        )

// replace the left-hand value for the right-hand one
operator fun YtFormat?.plus(rhs: YtFormat?): YtFormat? {
    if (this == null) return rhs
    if (rhs == null) return this
    return YtFormat(
            formatId = rhs.formatId ?: this.formatId,
            ext = rhs.ext ?: this.ext,
            width = rhs.width ?: this.width,
            height = rhs.height ?: this.height,
            formatNote = rhs.formatNote ?: this.formatNote,
            acodec = rhs.acodec ?: this.acodec,
            vcodec = rhs.vcodec ?: this.vcodec,
            fps = rhs.fps ?: this.fps,
            tbr = rhs.tbr ?: this.tbr,
            abr = rhs.abr ?: this.abr,
            asr = rhs.asr ?: this.asr,
            vbr = rhs.vbr ?: this.vbr,
            url = rhs.url ?: this.url,
            protocol = rhs.protocol ?: this.protocol,
            fileSize = rhs.fileSize ?: this.fileSize
    )
}