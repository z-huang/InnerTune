package com.zionhuang.music.viewmodels;

import android.app.Application;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.google.android.exoplayer2.ui.PlayerView;
import com.zionhuang.music.models.MediaData;
import com.zionhuang.music.playback.MediaSessionConnection;

public class PlaybackViewModel extends AndroidViewModel {
    private MediaSessionConnection mMediaSessionConnection;
    private LiveData<MediaControllerCompat> mMediaController;
    private MutableLiveData<MediaData> currentData = new MutableLiveData<>();

    //private Observer<PlaybackStateCompat> playbackStateObserver = playbackState -> currentData.postValue(currentData.getValue() != null ? currentData.getValue().pullPlaybackState(playbackState) : new MediaData().pullPlaybackState(playbackState));
    private Observer<MediaMetadataCompat> mediaMetadataObserver = mediaMetadata -> {
        MediaData newValue = currentData.getValue() != null ? currentData.getValue().pullMediaMetadata(mediaMetadata) : new MediaData().pullMediaMetadata(mediaMetadata);
        currentData.postValue(newValue);
    };

    public PlaybackViewModel(@NonNull Application application) {
        super(application);
        mMediaSessionConnection = new MediaSessionConnection(application);
        mMediaSessionConnection.connect();
        //mMediaSessionConnection.getPlaybackState().observeForever(playbackStateObserver);
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

    public LiveData<MediaData> getCurrentData() {
        return currentData;
    }

    public void playMedia(String videoId, Bundle extras) {
        mMediaSessionConnection.getTransportControls().playFromMediaId(videoId, extras);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mMediaSessionConnection.disconnect();
        //mMediaSessionConnection.getPlaybackState().removeObserver(playbackStateObserver);
        mMediaSessionConnection.getNowPlaying().removeObserver(mediaMetadataObserver);
    }
}
