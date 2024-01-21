package com.zionhuang.music.constants

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

val DynamicThemeKey = booleanPreferencesKey("dynamicTheme")
val DarkModeKey = stringPreferencesKey("darkMode")
val PureBlackKey = booleanPreferencesKey("pureBlack")
val BackgroundColorTintKey = booleanPreferencesKey("backgroundColorTint")
val DefaultOpenTabKey = stringPreferencesKey("defaultOpenTab")

const val SYSTEM_DEFAULT = "SYSTEM_DEFAULT"
val ContentLanguageKey = stringPreferencesKey("contentLanguage")
val ContentCountryKey = stringPreferencesKey("contentCountry")
val ProxyEnabledKey = booleanPreferencesKey("proxyEnabled")
val ProxyUrlKey = stringPreferencesKey("proxyUrl")
val ProxyTypeKey = stringPreferencesKey("proxyType")

val AudioQualityKey = stringPreferencesKey("audioQuality")

enum class AudioQuality {
    AUTO, HIGH, LOW
}

val PersistentQueueKey = booleanPreferencesKey("persistentQueue")
val SkipSilenceKey = booleanPreferencesKey("skipSilence")
val AudioNormalizationKey = booleanPreferencesKey("audioNormalization")

val MaxImageCacheSizeKey = intPreferencesKey("maxImageCacheSize")
val MaxSongCacheSizeKey = intPreferencesKey("maxSongCacheSize")

val PauseListenHistoryKey = booleanPreferencesKey("pauseListenHistory")
val PauseSearchHistoryKey = booleanPreferencesKey("pauseSearchHistory")
val EnableKugouKey = booleanPreferencesKey("enableKugou")

val SongSortTypeKey = stringPreferencesKey("songSortType")
val SongSortDescendingKey = booleanPreferencesKey("songSortDescending")
val PlaylistSongSortTypeKey = stringPreferencesKey("playlistSongSortType")
val PlaylistSongSortDescendingKey = booleanPreferencesKey("playlistSongSortDescending")
val ArtistSortTypeKey = stringPreferencesKey("artistSortType")
val ArtistSortDescendingKey = booleanPreferencesKey("artistSortDescending")
val AlbumSortTypeKey = stringPreferencesKey("albumSortType")
val AlbumSortDescendingKey = booleanPreferencesKey("albumSortDescending")
val PlaylistSortTypeKey = stringPreferencesKey("playlistSortType")
val PlaylistSortDescendingKey = booleanPreferencesKey("playlistSortDescending")
val ArtistSongSortTypeKey = stringPreferencesKey("artistSongSortType")
val ArtistSongSortDescendingKey = booleanPreferencesKey("artistSongSortDescending")

val SongFilterKey = stringPreferencesKey("songFilter")
val ArtistFilterKey = stringPreferencesKey("artistFilter")
val ArtistViewTypeKey = stringPreferencesKey("artistViewType")
val AlbumFilterKey = stringPreferencesKey("albumFilter")
val AlbumViewTypeKey = stringPreferencesKey("albumViewType")
val PlaylistViewTypeKey = stringPreferencesKey("playlistViewType")

val PlaylistEditLockKey = booleanPreferencesKey("playlistEditLock")

enum class LibraryViewType {
    LIST, GRID;

    fun toggle() = when (this) {
        LIST -> GRID
        GRID -> LIST
    }
}

enum class SongSortType {
    CREATE_DATE, NAME, ARTIST, PLAY_TIME
}

enum class PlaylistSongSortType {
    CUSTOM, CREATE_DATE, NAME, ARTIST, PLAY_TIME
}

enum class ArtistSortType {
    CREATE_DATE, NAME, SONG_COUNT, PLAY_TIME
}

enum class ArtistSongSortType {
    CREATE_DATE, NAME, PLAY_TIME
}

enum class AlbumSortType {
    CREATE_DATE, NAME, ARTIST, YEAR, SONG_COUNT, LENGTH, PLAY_TIME
}

enum class PlaylistSortType {
    CREATE_DATE, NAME, SONG_COUNT
}

enum class SongFilter {
    LIBRARY, LIKED, DOWNLOADED
}

enum class ArtistFilter {
    LIBRARY, LIKED
}

enum class AlbumFilter {
    LIBRARY, LIKED
}

val ShowLyricsKey = booleanPreferencesKey("showLyrics")
val LyricsTextPositionKey = stringPreferencesKey("lyricsTextPosition")
val TranslateLyricsKey = booleanPreferencesKey("translateLyrics")

val PlayerVolumeKey = floatPreferencesKey("playerVolume")
val RepeatModeKey = intPreferencesKey("repeatMode")

val SearchSourceKey = stringPreferencesKey("searchSource")

enum class SearchSource {
    LOCAL, ONLINE
}

val VisitorDataKey = stringPreferencesKey("visitorData")
val InnerTubeCookieKey = stringPreferencesKey("innerTubeCookie")
val AccountNameKey = stringPreferencesKey("accountName")
val AccountEmailKey = stringPreferencesKey("accountEmail")
val AccountChannelHandleKey = stringPreferencesKey("accountChannelHandle")

val LanguageCodeToName = mapOf(
    "af" to "Afrikaans",
    "az" to "Azərbaycan",
    "id" to "Bahasa Indonesia",
    "ms" to "Bahasa Malaysia",
    "ca" to "Català",
    "cs" to "Čeština",
    "da" to "Dansk",
    "de" to "Deutsch",
    "et" to "Eesti",
    "en-GB" to "English (UK)",
    "en" to "English (US)",
    "es" to "Español (España)",
    "es-419" to "Español (Latinoamérica)",
    "eu" to "Euskara",
    "fil" to "Filipino",
    "fr" to "Français",
    "fr-CA" to "Français (Canada)",
    "gl" to "Galego",
    "hr" to "Hrvatski",
    "zu" to "IsiZulu",
    "is" to "Íslenska",
    "it" to "Italiano",
    "sw" to "Kiswahili",
    "lt" to "Lietuvių",
    "hu" to "Magyar",
    "nl" to "Nederlands",
    "no" to "Norsk",
    "or" to "Odia",
    "uz" to "O‘zbe",
    "pl" to "Polski",
    "pt-PT" to "Português",
    "pt" to "Português (Brasil)",
    "ro" to "Română",
    "sq" to "Shqip",
    "sk" to "Slovenčina",
    "sl" to "Slovenščina",
    "fi" to "Suomi",
    "sv" to "Svenska",
    "bo" to "Tibetan བོད་སྐད།",
    "vi" to "Tiếng Việt",
    "tr" to "Türkçe",
    "bg" to "Български",
    "ky" to "Кыргызча",
    "kk" to "Қазақ Тілі",
    "mk" to "Македонски",
    "mn" to "Монгол",
    "ru" to "Русский",
    "sr" to "Српски",
    "uk" to "Українська",
    "el" to "Ελληνικά",
    "hy" to "Հայերեն",
    "iw" to "עברית",
    "ur" to "اردو",
    "ar" to "العربية",
    "fa" to "فارسی",
    "ne" to "नेपाली",
    "mr" to "मराठी",
    "hi" to "हिन्दी",
    "bn" to "বাংলা",
    "pa" to "ਪੰਜਾਬੀ",
    "gu" to "ગુજરાતી",
    "ta" to "தமிழ்",
    "te" to "తెలుగు",
    "kn" to "ಕನ್ನಡ",
    "ml" to "മലയാളം",
    "si" to "සිංහල",
    "th" to "ภาษาไทย",
    "lo" to "ລາວ",
    "my" to "ဗမာ",
    "ka" to "ქართული",
    "am" to "አማርኛ",
    "km" to "ខ្មែរ",
    "zh-CN" to "中文 (简体)",
    "zh-TW" to "中文 (繁體)",
    "zh-HK" to "中文 (香港)",
    "ja" to "日本語",
    "ko" to "한국어",
)

val CountryCodeToName = mapOf(
    "DZ" to "Algeria",
    "AR" to "Argentina",
    "AU" to "Australia",
    "AT" to "Austria",
    "AZ" to "Azerbaijan",
    "BH" to "Bahrain",
    "BD" to "Bangladesh",
    "BY" to "Belarus",
    "BE" to "Belgium",
    "BO" to "Bolivia",
    "BA" to "Bosnia and Herzegovina",
    "BR" to "Brazil",
    "BG" to "Bulgaria",
    "KH" to "Cambodia",
    "CA" to "Canada",
    "CL" to "Chile",
    "HK" to "Hong Kong",
    "CO" to "Colombia",
    "CR" to "Costa Rica",
    "HR" to "Croatia",
    "CY" to "Cyprus",
    "CZ" to "Czech Republic",
    "DK" to "Denmark",
    "DO" to "Dominican Republic",
    "EC" to "Ecuador",
    "EG" to "Egypt",
    "SV" to "El Salvador",
    "EE" to "Estonia",
    "FI" to "Finland",
    "FR" to "France",
    "GE" to "Georgia",
    "DE" to "Germany",
    "GH" to "Ghana",
    "GR" to "Greece",
    "GT" to "Guatemala",
    "HN" to "Honduras",
    "HU" to "Hungary",
    "IS" to "Iceland",
    "IN" to "India",
    "ID" to "Indonesia",
    "IQ" to "Iraq",
    "IE" to "Ireland",
    "IL" to "Israel",
    "IT" to "Italy",
    "JM" to "Jamaica",
    "JP" to "Japan",
    "JO" to "Jordan",
    "KZ" to "Kazakhstan",
    "KE" to "Kenya",
    "KR" to "South Korea",
    "KW" to "Kuwait",
    "LA" to "Lao",
    "LV" to "Latvia",
    "LB" to "Lebanon",
    "LY" to "Libya",
    "LI" to "Liechtenstein",
    "LT" to "Lithuania",
    "LU" to "Luxembourg",
    "MK" to "Macedonia",
    "MY" to "Malaysia",
    "MT" to "Malta",
    "MX" to "Mexico",
    "ME" to "Montenegro",
    "MA" to "Morocco",
    "NP" to "Nepal",
    "NL" to "Netherlands",
    "NZ" to "New Zealand",
    "NI" to "Nicaragua",
    "NG" to "Nigeria",
    "NO" to "Norway",
    "OM" to "Oman",
    "PK" to "Pakistan",
    "PA" to "Panama",
    "PG" to "Papua New Guinea",
    "PY" to "Paraguay",
    "PE" to "Peru",
    "PH" to "Philippines",
    "PL" to "Poland",
    "PT" to "Portugal",
    "PR" to "Puerto Rico",
    "QA" to "Qatar",
    "RO" to "Romania",
    "RU" to "Russian Federation",
    "SA" to "Saudi Arabia",
    "SN" to "Senegal",
    "RS" to "Serbia",
    "SG" to "Singapore",
    "SK" to "Slovakia",
    "SI" to "Slovenia",
    "ZA" to "South Africa",
    "ES" to "Spain",
    "LK" to "Sri Lanka",
    "SE" to "Sweden",
    "CH" to "Switzerland",
    "TW" to "Taiwan",
    "TZ" to "Tanzania",
    "TH" to "Thailand",
    "TN" to "Tunisia",
    "TR" to "Turkey",
    "UG" to "Uganda",
    "UA" to "Ukraine",
    "AE" to "United Arab Emirates",
    "GB" to "United Kingdom",
    "US" to "United States",
    "UY" to "Uruguay",
    "VE" to "Venezuela (Bolivarian Republic)",
    "VN" to "Vietnam",
    "YE" to "Yemen",
    "ZW" to "Zimbabwe",
)
