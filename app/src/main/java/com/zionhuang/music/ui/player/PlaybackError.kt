package com.zionhuang.music.ui.player

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.PlaybackException
import com.zionhuang.music.R
import com.zionhuang.music.playback.MusicService

@Composable
fun PlaybackError(
    error: PlaybackException,
    retry: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = { retry() }
            )
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.info),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            text = error.toUserMessage(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
private fun PlaybackException.toUserMessage(): String {
    return when (errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> stringResource(R.string.error_no_internet)
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> stringResource(R.string.error_timeout)
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
        PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED,
        PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED -> stringResource(R.string.error_unsupported_format)
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
        PlaybackException.ERROR_CODE_IO_NO_PERMISSION,
        PlaybackException.ERROR_CODE_BAD_VALUE -> stringResource(R.string.error_invalid_media_source)
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODING_FAILED -> stringResource(R.string.error_decoder_failed)
        MusicService.ERROR_CODE_NO_STREAM -> stringResource(R.string.error_no_stream)
        PlaybackException.ERROR_CODE_REMOTE_ERROR -> message ?: stringResource(R.string.error_stream_fetch_failed)
        else -> message ?: cause?.message ?: stringResource(R.string.error_unknown)
    }
}
