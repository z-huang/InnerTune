package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class YouTubeClient(
    val clientName: String,
    val clientVersion: String,
    // Numeric InnerTube client ID. YouTube's `X-YouTube-Client-Name` request header expects this
    // numeric ID, not the [clientName] string -- confirmed against two actively-maintained
    // InnerTube-derived forks (OuterTune, Metrolist) and yt-dlp's INNERTUBE_CLIENTS table, all of
    // which agree on these values. Sending the string name in that header (the previous behavior
    // here) produces a generic precondition-check rejection from music.youtube.com's player
    // endpoint.
    val clientId: String,
    val api_key: String,
    val userAgent: String,
    val osVersion: String? = null,
    val referer: String? = null,
    // Whether this client's player() request should carry a WebView-generated BotGuard PoToken
    // (see app-module PoTokenGenerator) in serviceIntegrityDimensions. Confirmed necessary on a
    // real device: only WEB_REMIX among our clients needs it -- IOS/ANDROID/TVHTML5 don't set
    // this and are unaffected by its absence.
    val useWebPoTokens: Boolean = false,
) {
    fun toContext(locale: YouTubeLocale, visitorData: String?) = Context(
        client = Context.Client(
            clientName = clientName,
            clientVersion = clientVersion,
            osVersion = osVersion,
            gl = locale.gl,
            hl = locale.hl,
            visitorData = visitorData
        )
    )

    companion object {
        private const val REFERER_YOUTUBE_MUSIC = "https://music.youtube.com/"

        private const val USER_AGENT_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36"
        private const val USER_AGENT_ANDROID = "com.google.android.youtube/21.26.364 (Linux; U; Android 11) gzip"
        private const val USER_AGENT_IOS = "com.google.ios.youtube/21.26.4 (iPhone16,2; U; CPU iOS 18_3_2 like Mac OS X;)"

        // Was previously the "ANDROID_MUSIC" client (clientVersion 5.01), which neither
        // OuterTune, Metrolist, nor yt-dlp's INNERTUBE_CLIENTS table still define -- it has
        // dropped out of the actively-maintained InnerTube ecosystem entirely. Repointed to plain
        // ANDROID (clientId 3), the client both of those forks use in its place, with a current
        // clientVersion/User-Agent cross-checked against yt-dlp. Field name kept as ANDROID_MUSIC
        // to avoid touching the fallback-chain call sites, which only care about its behavior
        // (age-restricted playback while logged in), not its literal identity string.
        val ANDROID_MUSIC = YouTubeClient(
            clientName = "ANDROID",
            clientVersion = "21.26.364",
            clientId = "3",
            api_key = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w",
            userAgent = USER_AGENT_ANDROID
        )

        val ANDROID = YouTubeClient(
            clientName = "ANDROID",
            clientVersion = "21.26.364",
            clientId = "3",
            api_key = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w",
            userAgent = USER_AGENT_ANDROID,
        )

        val WEB = YouTubeClient(
            clientName = "WEB",
            clientVersion = "2.20260708.00.00",
            clientId = "1",
            api_key = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX3",
            userAgent = USER_AGENT_WEB
        )

        val WEB_REMIX = YouTubeClient(
            clientName = "WEB_REMIX",
            clientVersion = "1.20260707.12.00",
            clientId = "67",
            api_key = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30",
            userAgent = USER_AGENT_WEB,
            referer = REFERER_YOUTUBE_MUSIC,
            useWebPoTokens = true,
        )

        // TVHTML5_SIMPLY_EMBEDDED_PLAYER (clientId 85): the embedded-player variant used together
        // with Context.ThirdParty.embedUrl in InnerTube.player() to bypass age-gating. The old
        // User-Agent here (AppleWebKit/601.2, a ~2016-era PS4 firmware string) is the kind of
        // stale UA YouTube's TV endpoint has been reported to reject outright with "YouTube is no
        // longer supported in this application or device" -- replaced with the current PS4
        // Safari/605.1 UA both Metrolist and yt-dlp's TV client family use today.
        val TVHTML5 = YouTubeClient(
            clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
            clientVersion = "2.0",
            clientId = "85",
            api_key = "AIzaSyDCU8hByM-4DrUqRUYnGn-3llEO78bcxq8",
            userAgent = "Mozilla/5.0 (PlayStation; PlayStation 4/12.02) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Safari/605.1.15"
        )

        val IOS = YouTubeClient(
            clientName = "IOS",
            clientVersion = "21.26.4",
            clientId = "5",
            api_key = "AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc",
            userAgent = USER_AGENT_IOS,
            osVersion = "18.3.2.22D82",
        )
    }
}
