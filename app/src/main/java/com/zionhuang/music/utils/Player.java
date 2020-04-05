package com.zionhuang.music.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.zionhuang.music.Extractor.YoutubeIE;
import com.zionhuang.music.Extractor.YtStream;
import com.zionhuang.music.ui.activities.MainActivity;

import java.util.EventListener;

/**
 * Player Singleton
 */
public class Player implements EventListener, com.google.android.exoplayer2.Player.EventListener {
    private static volatile Player mInstance;
    private DataSource.Factory dataSourceFactory;
    private ExoPlayer mPlayer;
    public MediaSource mediaSource;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    private Player(Context context, PlayerView playerView) {
        mPlayer = ExoPlayerFactory.newSimpleInstance(context);
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
                Log.d("Player", url);
            } else {
                Log.e("Player", "Extract error, msg: " + result.errorMessage);
            }
        }
    }

    private void load(MediaSource mediaSource) {
        mPlayer.prepare(mediaSource);
    }

    public void loadItem(Youtube.Item.Base item, Player player, Context context) {
        if (item instanceof Youtube.Item.Video) {
            Log.d("Player", "Video id: " + ((Youtube.Item.Video) item).getId());

            new ExtractAsyncTask(player).execute(((Youtube.Item.Video) item).getId());
        }
    }
}
