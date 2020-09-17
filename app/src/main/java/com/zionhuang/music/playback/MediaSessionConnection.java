package com.zionhuang.music.playback;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;

public class MediaSessionConnection {
    private Context mContext;
    private PlayerView mPlayerView;
    private MediaControllerCompat mMediaController;
    private MediaControllerCallback mMediaControllerCallback = new MediaControllerCallback();
    private MediaServiceConnection mServiceConnection;
    private MutableLiveData<Boolean> isConnected = new MutableLiveData<>();
    private MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData<>();
    private MutableLiveData<MediaMetadataCompat> nowPlaying = new MutableLiveData<>();

    public MediaSessionConnection(Context context) {
        mContext = context;
    }

    public void connect() {
        Intent intent = new Intent(mContext, MusicService.class);
        mServiceConnection = new MediaServiceConnection();
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void disconnect() {
        mContext.unbindService(mServiceConnection);
    }

    public void setPlayerView(PlayerView playerView) {
        mPlayerView = playerView;
    }

    @NonNull
    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    public LiveData<PlaybackStateCompat> getPlaybackState() {
        return playbackState;
    }

    public LiveData<MediaMetadataCompat> getNowPlaying() {
        return nowPlaying;
    }

    public MediaControllerCompat getMediaController() {
        return mMediaController;
    }

    @Nullable
    public MediaControllerCompat.TransportControls getTransportControls() {
        return mMediaController != null ? mMediaController.getTransportControls() : null;
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            playbackState.postValue(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            nowPlaying.postValue(metadata);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }
    }

    private class MediaServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService mService = ((MusicService.LocalBinder) service).getService();
            mService.setPlayerView(mPlayerView);
            try {
                mMediaController = new MediaControllerCompat(mContext, mService.getSessionToken());
                mMediaController.registerCallback(mMediaControllerCallback);
                isConnected.postValue(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
            isConnected.postValue(false);
        }
    }
}
