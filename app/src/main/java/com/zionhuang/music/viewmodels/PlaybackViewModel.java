package com.zionhuang.music.viewmodels;

import android.app.Application;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.google.android.exoplayer2.ui.PlayerView;
import com.zionhuang.music.models.MediaData;
import com.zionhuang.music.models.SongParcel;
import com.zionhuang.music.playback.MediaSessionConnection;
import com.zionhuang.music.playback.queue.Queue.Companion.QueueType;

import java.util.Objects;

import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;

public class PlaybackViewModel extends AndroidViewModel {
    private final MediaSessionConnection mMediaSessionConnection;
    private final LiveData<MediaControllerCompat> mMediaController;
    private final MutableLiveData<MediaData> currentSong = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentState = new MutableLiveData<>();

    private final Observer<PlaybackStateCompat> playbackStateObserver = playbackState -> currentState.postValue(playbackState.getState());
    private final Observer<MediaMetadataCompat> mediaMetadataObserver = mediaMetadata -> {
        MediaData newValue = currentSong.getValue() != null ? currentSong.getValue().pullMediaMetadata(mediaMetadata) : new MediaData().pullMediaMetadata(mediaMetadata);
        currentSong.postValue(newValue);
    };

    public PlaybackViewModel(@NonNull Application application) {
        super(application);
        mMediaSessionConnection = new MediaSessionConnection(application);
        mMediaSessionConnection.connect();
        mMediaSessionConnection.getPlaybackState().observeForever(playbackStateObserver);
        mMediaSessionConnection.getNowPlaying().observeForever(mediaMetadataObserver);
        mMediaController = Transformations.map(mMediaSessionConnection.getIsConnected(), isConnected -> {
            if (isConnected) {
                return mMediaSessionConnection.getMediaController();
            } else {
                return null;
            }
        });
    }

    public void setPlayerView(PlayerView playerView) {
        mMediaSessionConnection.setPlayerView(playerView);
    }

    public LiveData<MediaControllerCompat> getMediaController() {
        return mMediaController;
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        return mMediaSessionConnection.getTransportControls();
    }

    public LiveData<MediaData> getCurrentSong() {
        return currentSong;
    }

    public LiveData<Integer> getCurrentState() {
        return currentState;
    }

    public void togglePlayPause() {
        if (Objects.equals(currentState.getValue(), STATE_PLAYING)) {
            mMediaSessionConnection.getMediaController().getTransportControls().pause();
        } else {
            mMediaSessionConnection.getMediaController().getTransportControls().play();
        }
    }

    public void playMedia(@NonNull SongParcel song, @QueueType int queueType) {
        if (mMediaSessionConnection.getTransportControls() != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("song", song);
            bundle.putInt("queueType", queueType);
            mMediaSessionConnection.getTransportControls().playFromMediaId(song.getId(), bundle);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mMediaSessionConnection.disconnect();
        mMediaSessionConnection.getPlaybackState().removeObserver(playbackStateObserver);
        mMediaSessionConnection.getNowPlaying().removeObserver(mediaMetadataObserver);
    }
}
