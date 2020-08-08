package com.zionhuang.music.playback;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ui.PlayerView;

public class MusicService extends Service {
    private static final String TAG = "MusicService";
    private LocalBinder mBinder = new LocalBinder();
    private SongPlayer mSongPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mSongPlayer = new SongPlayer(this);
    }

    @Override
    public void onDestroy() {
        mSongPlayer.release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class LocalBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public MediaSessionCompat.Token getSessionToken() {
        return mSongPlayer.getMediaSession().getSessionToken();
    }

    public void setPlayerView(PlayerView playerView) {
        mSongPlayer.setPlayerView(playerView);
    }
}
