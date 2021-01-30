package com.zionhuang.music

import android.app.Application
import com.zionhuang.music.youtube.newpipe.NewPipeDownloader
import org.schabi.newpipe.extractor.NewPipe

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NewPipe.init(NewPipeDownloader.init())
    }
}