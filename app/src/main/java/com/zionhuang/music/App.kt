package com.zionhuang.music

import android.app.Application
import com.zionhuang.music.utils.getPreferredContentCountry
import com.zionhuang.music.utils.getPreferredLocalization
import com.zionhuang.music.youtube.NewPipeDownloader
import org.schabi.newpipe.extractor.NewPipe

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        NewPipe.init(
            NewPipeDownloader.init(),
            getPreferredLocalization(this),
            getPreferredContentCountry(this)
        )
    }

    companion object {
        lateinit var INSTANCE: App
    }
}