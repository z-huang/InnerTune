package com.zionhuang.music.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.zionhuang.music.extractor.YoutubeIE;
import com.zionhuang.music.extractor.YtStream;
import com.zionhuang.music.youtube.YtItem;
import com.zionhuang.music.ui.activities.MainActivity;

import java.util.EventListener;

/**
 * Player Singleton
 */
public class Player implements EventListener, com.google.android.exoplayer2.Player.EventListener {
    private static final String TAG = "Player";
    private static volatile Player mInstance;
    private DataSource.Factory dataSourceFactory;
    private ExoPlayer mPlayer;
    private final boolean playWhenReady = true;
    private final int currentWindow = 0;
    private final long playbackPosition = 0;

    private Player(Context context, PlayerView playerView) {
        mPlayer = new SimpleExoPlayer.Builder(context).build();
        playerView.setUseController(false);
        mPlayer.setPlayWhenReady(playWhenReady);
        mPlayer.seekTo(currentWindow, playbackPosition);
        playerView.setPlayer(mPlayer);
        dataSourceFactory = new DefaultDataSourceFactory(context, "music-client");
        mPlayer.addListener(this);
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        error.printStackTrace();
    }

    public static Player getInstance(Context context, PlayerView playerView) {
        if (mInstance == null) {
            synchronized (Player.class) {
                if (mInstance == null) {
                    mInstance = new Player(context, playerView);
                }
            }
        }
        return mInstance;
    }

    public static Player getInstance(Context context) {
        if (mInstance == null) {
            synchronized (Player.class) {
                if (mInstance == null) {
                    mInstance = new Player(context, ((MainActivity) context).getPlayerView());
                }
            }
        }
        return mInstance;
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    public static class ExtractAsyncTask extends AsyncTask<String, Void, YoutubeIE.Result> {
        private Player mPlayer;

        ExtractAsyncTask(Player player) {
            mPlayer = player;
        }

        @Override
        protected YoutubeIE.Result doInBackground(String... strings) {
            String videoId = strings[0];
            return new YoutubeIE().extract(videoId);
        }

        @Override
        protected void onPostExecute(YoutubeIE.Result result) {
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
                mPlayer.load(mPlayer.buildMediaSource(Uri.parse(url)));
                Log.d(TAG, url);
            } else {
                Log.e(TAG, "Extract error, msg: " + result.errorMessage);
            }
        }
    }

    private void load(MediaSource mediaSource) {
        mPlayer.prepare(mediaSource);
    }

    public void loadItem(YtItem.Base item, Player player, Context context) {
        if (item instanceof YtItem.Video) {
            Log.d(TAG, "Video id: " + ((YtItem.Video) item).getId());

            new ExtractAsyncTask(player).execute(((YtItem.Video) item).getId());
        }
    }
}
