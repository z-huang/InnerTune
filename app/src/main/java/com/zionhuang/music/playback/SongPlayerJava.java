package com.zionhuang.music.playback;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerView;
import com.zionhuang.music.R;
import com.zionhuang.music.db.SongEntity;
import com.zionhuang.music.extractor.YoutubeInfoExtractor;
import com.zionhuang.music.models.SongParcel;
import com.zionhuang.music.playback.queue.AllSongsQueue;
import com.zionhuang.music.playback.queue.EmptyQueue;
import com.zionhuang.music.playback.queue.Queue;
import com.zionhuang.music.playback.queue.Queue.Companion.QueueType;
import com.zionhuang.music.playback.queue.SingleSongQueue;
import com.zionhuang.music.repository.SongRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SEEK_TO;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_BUFFERING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_NONE;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;
import static com.google.android.exoplayer2.ui.PlayerNotificationManager.createWithNotificationChannel;
import static com.zionhuang.music.playback.queue.Queue.QUEUE_ALL_SONG;
import static com.zionhuang.music.playback.queue.Queue.QUEUE_SINGLE;

public class SongPlayerJava implements MusicPlayer.EventListener {
    private static final String TAG = "SongPlayer";
    private static final String CHANNEL_ID = "music_channel_01";
    private static final int NOTIFICATION_ID = 888;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private PlayerNotificationManager playerNotificationManager;
    private SongRepository mSongRepository;
    private MusicPlayer mMusicPlayer;
    private Queue mQueue = EmptyQueue.EMPTY_QUEUE;
    private MediaSessionCompat mMediaSession;

    SongPlayerJava(Context context) {
        mSongRepository = new SongRepository(context);
        mMusicPlayer = new MusicPlayer(context);
        mMusicPlayer.setListener(this);
        mMediaSession = new MediaSessionCompat(context, context.getString(R.string.app_name));
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        //mMediaSession.setCallback(new MediaSessionCallback(mMediaSession, this));
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(STATE_NONE, 0, 1F)
                .setActions(ACTION_PLAY | ACTION_PAUSE | ACTION_PLAY_PAUSE | ACTION_SEEK_TO)
                .build());
        mMediaSession.setActive(true);
        playerNotificationManager = createWithNotificationChannel(context, CHANNEL_ID, R.string.playback_channel_name, 0, NOTIFICATION_ID, new PlayerNotificationManager.MediaDescriptionAdapter() {
            @Override
            @NonNull
            public CharSequence getCurrentContentTitle(@NonNull Player player) {
                return getCurrentSong() != null ? (getCurrentSong().getTitle() != null ? getCurrentSong().getTitle() : "") : "";
            }

            @Nullable
            @Override
            public CharSequence getCurrentContentText(@NonNull Player player) {
                return getCurrentSong().getArtist();
            }

            @Nullable
            @Override
            public PendingIntent createCurrentContentIntent(@NonNull Player player) {
                return null;
            }


            @Nullable
            @Override
            public Bitmap getCurrentLargeIcon(@NonNull Player player, @NonNull PlayerNotificationManager.BitmapCallback callback) {
                return null;
            }
        });
        playerNotificationManager.setPlayer(mMusicPlayer.getExoPlayer());
        playerNotificationManager.setMediaSessionToken(mMediaSession.getSessionToken());
    }

    public MediaSessionCompat getMediaSession() {
        return mMediaSession;
    }

    public void play() {
        mMusicPlayer.play();
    }

    public void playSong() {
        mMusicPlayer.stop();
        SongEntity song = mQueue.getCurrentSong();
        if (song == null) {
            return;
        }
        updateMetadata(song.getTitle(), song.getArtist(), song.getDuration());
        Disposable d = Observable.just(song.getId())
                .map(id -> new YoutubeInfoExtractor().extract(id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.success) {
                        updateMetadata(result.getSong().getTitle(), result.getSong().getArtist(), result.getSong().getDuration() * 1000);
                        mSongRepository.insert(result.getSong());
                        String url;
                        if (result.hasNormalStream()) {
                            url = result.getNormalStream().url;
                        } else {
                            url = result.getVideoStream().url;
                        }
                        mMusicPlayer.setSource(Uri.parse(url));
                    } else {
                        Log.w(TAG, "Extraction failed.\nError code: " + result.getErrorCode().toString() + "\nMessage: " + result.getErrorMessage());
                    }
                }, Throwable::printStackTrace);
        compositeDisposable.add(d);
    }

    public void pause() {
        mMusicPlayer.pause();
        if (mMusicPlayer.isPlaying()) {
            mMusicPlayer.pause();
            updatePlaybackState(STATE_PAUSED, mMediaSession.getController().getPlaybackState().getPosition(), mMusicPlayer.getPlaybackSpeed());
        }
    }

    public void seekTo(long pos) {
        mMusicPlayer.seekTo(pos);
        updatePlaybackState(mMediaSession.getController().getPlaybackState().getState(), pos, mMusicPlayer.getPlaybackSpeed());
    }

    public void setQueue(@QueueType int queueType, String currentSongId) {
        switch (queueType) {
            case QUEUE_ALL_SONG:
                mQueue = new AllSongsQueue(mSongRepository);
                break;
            case QUEUE_SINGLE:
                mQueue = new SingleSongQueue(mSongRepository, currentSongId);
        }
        mQueue.setCurrentSongId(currentSongId);
    }

    public SongEntity getCurrentSong() {
        return mQueue.getCurrentSong();
    }

    public void playNext() {
        mQueue.playNext();
        playSong();
    }

    public void playPrevious() {
        mQueue.playPrevious();
        playSong();
    }

    private void repeatSong() {

    }

    private void repeatQueue() {

    }

    public void stop() {
        mMusicPlayer.stop();
        updatePlaybackState(STATE_NONE, 0, mMusicPlayer.getPlaybackSpeed());
    }

    public void release() {
        mMediaSession.setActive(false);
        mMediaSession.release();
        playerNotificationManager.setPlayer(null);
        mMusicPlayer.release();
        compositeDisposable.dispose();
    }

    public void setPlayerView(PlayerView playerView) {
        mMusicPlayer.setPlayerView(playerView);
    }

    private void updateMetadata(String title, String artist, long duration) {
        MediaMetadataCompat currentMetadata = mMediaSession.getController().getMetadata();
        setMetadata((currentMetadata == null ? new MediaMetadataCompat.Builder() : new MediaMetadataCompat.Builder(currentMetadata))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build());
    }

    private void setMetadata(MediaMetadataCompat metadata) {
        mMediaSession.setMetadata(metadata);
    }

    public void updatePlaybackState(@PlaybackStateCompat.State int state, long position, float playbackSpeed) {
        setPlaybackState(new PlaybackStateCompat.Builder(mMediaSession.getController().getPlaybackState())
                .setState(state, position, playbackSpeed)
                .build());
    }

    public void setPlaybackState(PlaybackStateCompat state) {
        mMediaSession.setPlaybackState(state);
    }

    @Override
    public void onDurationSet(long duration) {
        setMetadata(new MediaMetadataCompat.Builder(mMediaSession.getController().getMetadata())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build());
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        int state = STATE_NONE;
        switch (playbackState) {
            case Player.STATE_IDLE:
                state = STATE_NONE;
                break;
            case Player.STATE_BUFFERING:
                state = STATE_BUFFERING;
                break;
            case Player.STATE_READY:
                state = mMusicPlayer.isPlaying() ? STATE_PLAYING : STATE_PAUSED;
                break;
            case Player.STATE_ENDED:
                playNext();
                break;
        }
        updatePlaybackState(state, mMusicPlayer.getPosition(), mMusicPlayer.getPlaybackSpeed());
    }

    public void updateSongMeta(String songId, SongParcel songParcel) {
        mQueue.updateSongMeta(songId, songParcel);
    }

    public void addToLibrary() {
        String id = getCurrentSong().getId();
    }
}
