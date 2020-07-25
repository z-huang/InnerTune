package com.zionhuang.music.playback;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.zionhuang.music.R;
import com.zionhuang.music.extractor.YoutubeIE;
import com.zionhuang.music.extractor.YtStream;

import java.lang.ref.WeakReference;
import java.util.Objects;

import static com.google.android.exoplayer2.Player.STATE_READY;

public class MusicService extends Service implements PlayerNotificationManager.MediaDescriptionAdapter, PlayerNotificationManager.NotificationListener, AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "MusicService";
    public static final String PLAYBACK_CHANNEL_ID = "music_channel_01";
    public static final int PLAYBACK_NOTIFICATION_ID = 888;
    public static final String ACTION_PLAY_PAUSE = "action_play_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";

    private LocalBinder mBinder = new LocalBinder();
    private YoutubeIE mYoutubeIE = new YoutubeIE();
    private SimpleExoPlayer mPlayer;
    private DataSource.Factory dataSourceFactory;
    private PlayerNotificationManager playerNotificationManager;

    private MediaSessionCompat mMediaSession;
    private final AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(this)
            .build();

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Objects.equals(intent.getAction(), Intent.ACTION_MEDIA_BUTTON)) {
                return;
            }
            Bundle extras = intent.getExtras();
            if (extras == null) {
                return;
            }
            KeyEvent keyEvent = (KeyEvent) extras.get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent != null) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    case KeyEvent.KEYCODE_MEDIA_STOP:
                        break;
                }
            }
        }
    };

    private static class ExtractAsyncTask extends AsyncTask<String, Void, YoutubeIE.Result> {
        private WeakReference<MusicService> mService;

        ExtractAsyncTask(MusicService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        protected YoutubeIE.Result doInBackground(String... strings) {
            MusicService service = mService.get();
            if (service == null) {
                return null;
            }
            String videoId = strings[0];
            return service.mYoutubeIE.extract(videoId);
        }

        @Override
        protected void onPostExecute(YoutubeIE.Result result) {
            MusicService service = mService.get();
            if (service == null || result == null) {
                return;
            }
            if (result.success) {
                String url;
                YtStream stream;
                if (result.hasNormalStream()) {
                    stream = result.getBestNormal();
                    url = result.getBestNormal().url;
                } else {
                    stream = result.getBestVideo();
                    url = result.getBestVideo().url;
                }

                service.mPlayer.prepare(service.buildMediaSource(Uri.parse(url)));
                PlaybackStateCompat playbackState = service.mMediaSession.getController().getPlaybackState();
                service.mMediaSession.setPlaybackState(
                        new PlaybackStateCompat.Builder(service.mMediaSession.getController().getPlaybackState())
                                .setState(PlaybackStateCompat.STATE_PLAYING, playbackState != null ? playbackState.getPosition() : 0L, 1F)
                                .build());
                Log.d(TAG, url);
            } else {
                Log.e(TAG, "Extract error, msg: " + result.errorMessage);
            }
        }
    }

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, extras.getString("title"))
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, extras.getString("artist"))
                    .build());
            new ExtractAsyncTask(MusicService.this).execute(mediaId);
        }

        @Override
        public void onPlay() {
            super.onPlay();
            if (!requestAudioFocus()) {
                return;
            }
            mMediaSession.setActive(true);
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
        }

        @Override
        public void onPause() {
            super.onPause();
            if (mPlayer.getPlayWhenReady()) {
                mPlayer.setPlayWhenReady(false);
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            }
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            Log.d(TAG, "seekTo: "+pos);
            mPlayer.seekTo(pos);
        }
    };

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mPlayer.getPlayWhenReady()) {
                    mPlayer.setPlayWhenReady(false);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mPlayer != null) {
                    mPlayer.setVolume(0.3f);
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mPlayer != null) {
                    if (!mPlayer.getPlayWhenReady()) {
                        mPlayer.setPlayWhenReady(true);
                    }
                    mPlayer.setVolume(1.0f);
                }
                break;
        }
    }

    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder();
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSession.setPlaybackState(playbackStateBuilder.build());
    }

    private boolean requestAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return false;
        }

        int res = audioManager.requestAudioFocus(audioFocusRequest);
        return res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void initPlayer() {
        mPlayer = new SimpleExoPlayer.Builder(this).build();
        mPlayer.setForegroundMode(true);
        mPlayer.setHandleWakeLock(true);
        mPlayer.setPlayWhenReady(true);
        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)));
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(this, PLAYBACK_CHANNEL_ID, R.string.playback_channel_name, 0, PLAYBACK_NOTIFICATION_ID, this, this);
        playerNotificationManager.setPlayer(mPlayer);
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == STATE_READY) {
                    mMediaSession.setMetadata(
                            new MediaMetadataCompat.Builder(mMediaSession.getController().getMetadata())
                                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mPlayer.getDuration())
                                    .build());
                    Log.d(TAG, "duration: " + mPlayer.getDuration());
                }
                if (playWhenReady) {

                } else {

                }
            }
        });
    }

    private void initMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSession = new MediaSessionCompat(getApplicationContext(), getString(R.string.app_name), mediaButtonReceiver, null);
        mMediaSession.setCallback(mMediaSessionCallback);
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setActive(true);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSession.setMediaButtonReceiver(pendingIntent);
    }

    private void initNoisyReceiver() {
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initPlayer();
        initMediaSession();
        initNoisyReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }

        switch (intent.getAction()) {
            case ACTION_PLAY_PAUSE:
                mPlayer.setPlayWhenReady(!mPlayer.getPlayWhenReady());
                break;
            case ACTION_NEXT:
                break;
            case ACTION_PREVIOUS:
                break;
        }
        MediaButtonReceiver.handleIntent(mMediaSession, intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        }
        unregisterReceiver(mNoisyReceiver);
        mMediaSession.release();
        playerNotificationManager.setPlayer(null);
        mPlayer.release();
        mPlayer = null;
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

    @Override
    public String getCurrentContentTitle(Player player) {
        return null;
    }

    @Nullable
    @Override
    public PendingIntent createCurrentContentIntent(Player player) {
        return null;
    }

    @Nullable
    @Override
    public String getCurrentContentText(Player player) {
        return null;
    }

    @Nullable
    @Override
    public String getCurrentSubText(Player player) {
        return null;
    }

    @Nullable
    @Override
    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
        return null;
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    public void setPlayerView(PlayerView playerView) {
        playerView.setPlayer(mPlayer);
    }

    public MediaSessionCompat.Token getSessionToken() {
        return mMediaSession.getSessionToken();
    }
}
