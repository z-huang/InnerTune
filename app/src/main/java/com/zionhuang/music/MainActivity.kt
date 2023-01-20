package com.zionhuang.music

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.valentinilk.shimmer.LocalShimmerTheme
import com.zionhuang.music.constants.*
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.entities.SearchHistory
import com.zionhuang.music.extensions.*
import com.zionhuang.music.playback.MusicService
import com.zionhuang.music.playback.MusicService.MusicBinder
import com.zionhuang.music.playback.PlayerConnection
import com.zionhuang.music.ui.component.*
import com.zionhuang.music.ui.component.shimmer.ShimmerTheme
import com.zionhuang.music.ui.player.BottomSheetPlayer
import com.zionhuang.music.ui.screens.*
import com.zionhuang.music.ui.screens.library.LibraryAlbumsScreen
import com.zionhuang.music.ui.screens.library.LibraryArtistsScreen
import com.zionhuang.music.ui.screens.library.LibraryPlaylistsScreen
import com.zionhuang.music.ui.screens.library.LibrarySongsScreen
import com.zionhuang.music.ui.screens.settings.*
import com.zionhuang.music.ui.theme.*
import com.zionhuang.music.utils.dataStore
import com.zionhuang.music.utils.get
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var database: MusicDatabase
    private var playerConnection by mutableStateOf<PlayerConnection?>(null)
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MusicBinder) {
                playerConnection = PlayerConnection(database, service)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerConnection?.dispose()
            playerConnection = null
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, MusicService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val coroutineScope = rememberCoroutineScope()
            val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
            val isSystemInDarkTheme = isSystemInDarkTheme()
            val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
                if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
            }
            LaunchedEffect(useDarkTheme) {
                setSystemBarAppearance(useDarkTheme)
            }
            var themeColor by rememberSaveable(stateSaver = ColorSaver) {
                mutableStateOf(DefaultThemeColor)
            }

            DisposableEffect(playerConnection?.binder, isSystemInDarkTheme) {
                playerConnection?.onBitmapChanged = { bitmap ->
                    coroutineScope.launch(Dispatchers.IO) {
                        themeColor = bitmap?.extractThemeColor() ?: DefaultThemeColor
                    }
                }
                onDispose {
                    playerConnection?.onBitmapChanged = {}
                }
            }

            InnerTuneTheme(
                darkTheme = useDarkTheme,
                themeColor = themeColor
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val route = remember(navBackStackEntry) { navBackStackEntry?.destination?.route }

                    val navigationItems = remember {
                        listOf(Screen.Home, Screen.Songs, Screen.Artists, Screen.Albums, Screen.Playlists)
                    }
                    val defaultOpenTab = remember {
                        dataStore[DefaultOpenTabKey].toEnum(defaultValue = NavigationTab.HOME)
                    }

                    val (isSearchExpanded, onSearchExpandedChange) = rememberSaveable { mutableStateOf(false) }
                    val (textFieldValue, onTextFieldValueChange) = rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
                    val appBarConfig = remember(navBackStackEntry) {
                        when {
                            route == null || navigationItems.any { it.route == route } -> searchAppBarConfig()
                            route.startsWith("search/") -> onlineSearchResultAppBarConfig(navBackStackEntry?.arguments?.getString("query").orEmpty())
                            route.startsWith("album/") -> albumAppBarConfig()
                            route.startsWith("artist/") -> artistAppBarConfig()
                            route.startsWith("playlist/") -> playlistAppBarConfig()
                            route.startsWith("settings") -> settingsAppBarConfig(route)
                            else -> defaultAppBarConfig()
                        }
                    }
                    val onSearch: (String) -> Unit = remember {
                        { query ->
                            onTextFieldValueChange(TextFieldValue(
                                text = query,
                                selection = TextRange(query.length)
                            ))
                            navController.navigate("search/$query")
                            database.query {
                                insert(SearchHistory(query = query))
                            }
                        }
                    }

                    val shouldShowNavigationBar = remember(navBackStackEntry, isSearchExpanded) {
                        route == null || navigationItems.fastAny { it.route == route } && !isSearchExpanded
                    }
                    val navigationBarHeight by animateDpAsState(
                        targetValue = if (shouldShowNavigationBar) NavigationBarHeight else 0.dp,
                        animationSpec = NavigationBarAnimationSpec
                    )

                    val density = LocalDensity.current
                    val windowsInsets = WindowInsets.systemBars
                    val bottomInset = with(density) { windowsInsets.getBottom(density).toDp() }

                    val playerBottomSheetState = rememberBottomSheetState(
                        dismissedBound = 0.dp,
                        collapsedBound = bottomInset + (if (shouldShowNavigationBar) NavigationBarHeight else 0.dp) + MiniPlayerHeight,
                        expandedBound = maxHeight,
                    )

                    val playerAwareWindowInsets = remember(bottomInset, shouldShowNavigationBar, playerBottomSheetState.isDismissed) {
                        var bottom = bottomInset
                        if (shouldShowNavigationBar) bottom += NavigationBarHeight
                        if (!playerBottomSheetState.isDismissed) bottom += MiniPlayerHeight
                        windowsInsets
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                            .add(WindowInsets(
                                top = AppBarHeight,
                                bottom = bottom
                            ))
                    }

                    val scrollBehavior = appBarScrollBehavior(
                        canScroll = {
                            route?.startsWith("search/") == false &&
                                    (playerBottomSheetState.isCollapsed || playerBottomSheetState.isDismissed)
                        }
                    )

                    LaunchedEffect(route) {
                        onSearchExpandedChange(false)
                        if (navigationItems.any { it.route == route }) {
                            onTextFieldValueChange(TextFieldValue())
                        }

                        val heightOffset = scrollBehavior.state.heightOffset
                        animate(
                            initialValue = heightOffset,
                            targetValue = 0f
                        ) { value, _ ->
                            scrollBehavior.state.heightOffset = value
                        }
                    }

                    LaunchedEffect(playerConnection) {
                        val player = playerConnection?.player ?: return@LaunchedEffect
                        if (player.currentMediaItem == null) {
                            if (!playerBottomSheetState.isDismissed) {
                                playerBottomSheetState.dismiss()
                            }
                        } else {
                            if (playerBottomSheetState.isDismissed) {
                                playerBottomSheetState.collapseSoft()
                            }
                        }
                    }

                    val expandOnPlay by rememberPreference(ExpandOnPlayKey, defaultValue = false)

                    DisposableEffect(playerConnection, playerBottomSheetState) {
                        val player = playerConnection?.player ?: return@DisposableEffect onDispose { }
                        val listener = object : Player.Listener {
                            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && mediaItem != null && playerBottomSheetState.isDismissed) {
                                    if (expandOnPlay) {
                                        playerBottomSheetState.expandSoft()
                                    } else {
                                        playerBottomSheetState.collapseSoft()
                                    }
                                }
                            }
                        }
                        player.addListener(listener)
                        onDispose {
                            player.removeListener(listener)
                        }
                    }

                    CompositionLocalProvider(
                        LocalDatabase provides database,
                        LocalContentColor provides contentColorFor(MaterialTheme.colorScheme.background),
                        LocalPlayerConnection provides playerConnection,
                        LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                        LocalShimmerTheme provides ShimmerTheme
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = when (defaultOpenTab) {
                                NavigationTab.HOME -> Screen.Home
                                NavigationTab.SONG -> Screen.Songs
                                NavigationTab.ARTIST -> Screen.Artists
                                NavigationTab.ALBUM -> Screen.Albums
                                NavigationTab.PLAYLIST -> Screen.Playlists
                            }.route,
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        ) {
                            composable(Screen.Home.route) {
                                HomeScreen(navController)
                            }
                            composable(Screen.Songs.route) {
                                LibrarySongsScreen(navController)
                            }
                            composable(Screen.Artists.route) {
                                LibraryArtistsScreen(navController)
                            }
                            composable(Screen.Albums.route) {
                                LibraryAlbumsScreen(navController)
                            }
                            composable(Screen.Playlists.route) {
                                LibraryPlaylistsScreen(navController)
                            }
                            composable(
                                route = "album/{albumId}?playlistId={playlistId}",
                                arguments = listOf(
                                    navArgument("albumId") {
                                        type = NavType.StringType
                                    },
                                    navArgument("playlistId") {
                                        type = NavType.StringType
                                        nullable = true
                                    }
                                )
                            ) {
                                AlbumScreen(navController)
                            }
                            composable(
                                route = "artist/{artistId}",
                                arguments = listOf(
                                    navArgument("artistId") {
                                        type = NavType.StringType
                                    }
                                )
                            ) {
                                ArtistScreen(
                                    navController = navController,
                                    appBarConfig = appBarConfig
                                )
                            }
                            composable(
                                route = "artistItems/{browseId}?params={params}",
                                arguments = listOf(
                                    navArgument("browseId") {
                                        type = NavType.StringType
                                    },
                                    navArgument("params") {
                                        type = NavType.StringType
                                        nullable = true
                                    }
                                )
                            ) {
                                ArtistItemsScreen(
                                    navController = navController,
                                    appBarConfig = appBarConfig
                                )
                            }
                            composable(
                                route = "playlist/{playlistId}",
                                arguments = listOf(
                                    navArgument("playlistId") {
                                        type = NavType.StringType
                                    }
                                )
                            ) {
                                LocalPlaylistScreen()
                            }
                            composable(
                                route = "search/{query}",
                                arguments = listOf(
                                    navArgument("query") {
                                        type = NavType.StringType
                                    }
                                )
                            ) {
                                OnlineSearchResult(
                                    navController = navController
                                )
                            }

                            composable("settings") {
                                SettingsScreen(navController)
                            }
                            composable("settings/appearance") {
                                AppearanceSettings()
                            }
                            composable("settings/content") {
                                ContentSettings()
                            }
                            composable("settings/player") {
                                PlayerSettings()
                            }
                            composable("settings/storage") {
                                StorageSettings()
                            }
                            composable("settings/general") {
                                GeneralSettings()
                            }
                            composable("settings/privacy") {
                                PrivacySettings()
                            }
                            composable("settings/backup_restore") {
                                BackupAndRestore()
                            }
                            composable("settings/about") {
                                AboutScreen()
                            }
                        }

                        AppBar(
                            appBarConfig = appBarConfig,
                            textFieldValue = textFieldValue,
                            onTextFieldValueChange = onTextFieldValueChange,
                            isSearchExpanded = isSearchExpanded,
                            onSearchExpandedChange = onSearchExpandedChange,
                            scrollBehavior = scrollBehavior,
                            navController = navController,
                            localSearchScreen = { query, _ ->
                                LocalSearchScreen(
                                    query = query,
                                    navController = navController
                                )
                            },
                            onlineSearchScreen = { query, onDismiss ->
                                OnlineSearchScreen(
                                    query = query,
                                    onTextFieldValueChange = onTextFieldValueChange,
                                    navController = navController,
                                    onSearch = onSearch,
                                    onDismiss = onDismiss
                                )
                            },
                            onSearchOnline = onSearch
                        )

                        BottomSheetPlayer(
                            state = playerBottomSheetState,
                            navController = navController
                        )

                        NavigationBar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset {
                                    if (navigationBarHeight == 0.dp) {
                                        IntOffset(x = 0, y = (bottomInset + NavigationBarHeight).roundToPx())
                                    } else {
                                        val slideOffset = (bottomInset + NavigationBarHeight) * playerBottomSheetState.progress.coerceIn(0f, 1f)
                                        val hideOffset = (bottomInset + NavigationBarHeight) * (1 - navigationBarHeight / NavigationBarHeight)
                                        IntOffset(
                                            x = 0,
                                            y = (slideOffset + hideOffset).roundToPx()
                                        )
                                    }
                                }
                        ) {
                            navigationItems.fastForEach { screen ->
                                NavigationBarItem(
                                    selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true,
                                    icon = {
                                        Icon(
                                            painter = painterResource(screen.iconId),
                                            contentDescription = null
                                        )
                                    },
                                    label = { Text(stringResource(screen.titleId)) },
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }

                        BottomSheetMenu(
                            state = LocalMenuState.current,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setSystemBarAppearance(isDark: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView.rootView).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            window.navigationBarColor = (if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.2f)).toArgb()
        }
    }
}

const val ACTION_SHOW_BOTTOM_SHEET = "show_bottom_sheet"

val LocalDatabase = staticCompositionLocalOf<MusicDatabase> { error("No database provided") }
val LocalPlayerConnection = staticCompositionLocalOf<PlayerConnection?> { error("No PlayerConnection provided") }
val LocalPlayerAwareWindowInsets = compositionLocalOf<WindowInsets> { error("No WindowInsets provided") }
