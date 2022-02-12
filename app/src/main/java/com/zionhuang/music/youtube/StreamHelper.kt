package com.zionhuang.music.youtube

import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.MediaFormat.*
import org.schabi.newpipe.extractor.stream.AudioStream

object StreamHelper {
    // Audio format in order of quality. 0=lowest quality, n=highest quality
    private val AUDIO_FORMAT_QUALITY_RANKING = listOf(MP3, WEBMA, M4A)
    // Audio format in order of efficiency. 0=least efficient, n=most efficient
    private val AUDIO_FORMAT_EFFICIENCY_RANKING = listOf(MP3, M4A, WEBMA)

    private fun compareAudioStreamBitrate(streamA: AudioStream, streamB: AudioStream, formatRanking: List<MediaFormat>) = if (streamA.averageBitrate != streamB.averageBitrate)
        streamA.averageBitrate - streamB.averageBitrate
    else formatRanking.indexOf(streamB.getFormat()) - formatRanking.indexOf(streamB.getFormat())

    fun getHighestQualityAudioStream(audioStreams: List<AudioStream>): AudioStream? = audioStreams.sortedWith { a, b ->
        compareAudioStreamBitrate(a, b, AUDIO_FORMAT_QUALITY_RANKING)
    }.lastOrNull()

    fun getMostCompactAudioStream(audioStreams: List<AudioStream>): AudioStream? = audioStreams.sortedWith { a, b ->
        compareAudioStreamBitrate(a, b, AUDIO_FORMAT_EFFICIENCY_RANKING)
    }.firstOrNull()

}