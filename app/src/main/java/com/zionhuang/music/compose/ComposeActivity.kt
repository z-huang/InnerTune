package com.zionhuang.music.compose

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.palette.graphics.Palette
import com.google.android.exoplayer2.Player.STATE_IDLE
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.defaultShimmerTheme
import com.zionhuang.music.R
import com.zionhuang.music.compose.component.AppBar
import com.zionhuang.music.compose.component.AppBarConfig
import com.zionhuang.music.compose.component.appBarScrollBehavior
import com.zionhuang.music.compose.component.rememberBottomSheetState
import com.zionhuang.music.compose.player.BottomSheetPlayer
import com.zionhuang.music.compose.screens.*
import com.zionhuang.music.compose.screens.library.LibraryAlbumsScreen
import com.zionhuang.music.compose.screens.library.LibraryArtistsScreen
import com.zionhuang.music.compose.screens.library.LibraryPlaylistsScreen
import com.zionhuang.music.compose.screens.library.LibrarySongsScreen
import com.zionhuang.music.compose.theme.ColorSaver
import com.zionhuang.music.compose.theme.DefaultThemeColor
import com.zionhuang.music.compose.theme.InnerTuneTheme
import com.zionhuang.music.compose.utils.rememberPreference
import com.zionhuang.music.constants.*
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.playback.MusicService
import com.zionhuang.music.playback.MusicService.MusicBinder
import com.zionhuang.music.playback.PlayerConnection
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.utils.NavigationTabHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ComposeActivity : ComponentActivity() {
    private val playerConnection = PlayerConnection(this)
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MusicBinder) {
                playerConnection.init(service)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerConnection.dispose()
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
            val isSystemInDarkTheme = isSystemInDarkTheme()
            var themeColor by rememberSaveable(stateSaver = ColorSaver) {
                mutableStateOf(DefaultThemeColor)
            }

            DisposableEffect(playerConnection.binder, isSystemInDarkTheme) {
                playerConnection.onBitmapChanged = { bitmap ->
                    if (bitmap != null) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val palette = Palette.from(bitmap).maximumColorCount(8).generate()
                            val defaultHsl = palette.dominantSwatch!!.hsl
                            val hsl = defaultHsl.takeIf { it[1] >= 0.08 }
                                ?: palette.swatches
                                    .map { it.hsl }
                                    .filter { it[1] != 0f }
                                    .maxByOrNull { it[1] }
                                ?: defaultHsl
                            themeColor = Color.hsl(hsl[0], hsl[1], hsl[2])
                        }
                    } else {
                        themeColor = DefaultThemeColor
                    }
                }

                onDispose {
                    playerConnection.onBitmapChanged = {}
                }
            }

            InnerTuneTheme(
                darkTheme = isSystemInDarkTheme,
                dynamicColor = sharedPreferences.getBoolean(getString(R.string.pref_follow_system_accent), true),
                themeColor = themeColor
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val density = LocalDensity.current
                    val windowsInsets = WindowInsets.systemBars
                    val bottomInset = with(density) { windowsInsets.getBottom(density).toDp() }
                    val playerBottomSheetState = rememberBottomSheetState(
                        dismissedBound = 0.dp,
                        collapsedBound = NavigationBarHeight.dp + MiniPlayerHeight.dp + bottomInset,
                        expandedBound = maxHeight,
                    )

                    val playbackState by playerConnection.playbackState.collectAsState(STATE_IDLE)
                    LaunchedEffect(playbackState) {
                        if (playbackState == STATE_IDLE) {
                            if (!playerBottomSheetState.isDismissed) {
                                playerBottomSheetState.dismiss()
                            }
                        } else {
                            if (playerBottomSheetState.isDismissed) {
                                playerBottomSheetState.collapseSoft()
                            }
                        }
                    }

                    val playerAwareWindowInsets by remember(bottomInset, playerBottomSheetState.value) {
                        derivedStateOf {
//                        val bottom = if (playerBottomSheetState.isDismissed) {
//                            NavigationBarHeight.dp + bottomInset
//                        } else {
//                            playerBottomSheetState.collapsedBound
//                        }
                            val bottom = playerBottomSheetState.value.coerceIn(NavigationBarHeight.dp + bottomInset, playerBottomSheetState.collapsedBound)

                            windowsInsets
                                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                                .add(WindowInsets(
                                    top = AppBarHeight.dp,
                                    bottom = bottom
                                ))
                        }
                    }

                    val shimmerTheme = remember {
                        defaultShimmerTheme.copy(
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 800,
                                    easing = LinearEasing,
                                    delayMillis = 250,
                                ),
                                repeatMode = RepeatMode.Restart
                            ),
                            shaderColors = listOf(
                                Color.Unspecified.copy(alpha = 0.25f),
                                Color.Unspecified.copy(alpha = 0.50f),
                                Color.Unspecified.copy(alpha = 0.25f),
                            ),
                        )
                    }

                    CompositionLocalProvider(
                        LocalPlayerConnection provides playerConnection,
                        LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                        LocalShimmerTheme provides shimmerTheme
                    ) {
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val route = remember(navBackStackEntry) {
                            navBackStackEntry?.destination?.route
                        }

                        val navigationItems = listOf(Screen.Home, Screen.Songs, Screen.Artists, Screen.Albums, Screen.Playlists)
                        val defaultNavIndex = sharedPreferences.getString(getString(R.string.pref_default_open_tab), "0")!!.toInt()
                        val enabledNavItems = NavigationTabHelper.getConfig(this@ComposeActivity)

                        val (textFieldValue, onTextFieldValueChange) = rememberSaveable(stateSaver = TextFieldValue.Saver) {
                            mutableStateOf(
                                TextFieldValue("")
                            )
                        }
                        var searchSource by rememberPreference(SEARCH_SOURCE, ONLINE)
                        val appBarConfig = remember(route) {
                            route ?: return@remember AppBarConfig(navigationIcon = R.drawable.ic_search)
                            if (navigationItems.any { it.route == route }) {
                                return@remember AppBarConfig(
                                    title = {
                                        Text(
                                            text = stringResource(R.string.menu_search),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .alpha(0.6f)
                                                .weight(1f)
                                        )
                                    },
                                    navigationIcon = R.drawable.ic_search,
                                    onNavigationButtonClick = {
                                        navController.navigate("search")
                                    },
                                    canSearch = true
                                )
                            }
                            when {
                                route == "search" -> AppBarConfig(
                                    onNavigationButtonClick = {
                                        navController.navigateUp()
                                        onTextFieldValueChange(TextFieldValue(""))
                                    },
                                    canSearch = true,
                                    searchExpanded = true,
                                    actions = {
                                        IconButton(
                                            onClick = {
                                                searchSource = if (searchSource == ONLINE) LOCAL else ONLINE
                                            },
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(if (searchSource == ONLINE) R.drawable.ic_language else R.drawable.ic_library_music),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                )
                                route.startsWith("search/") -> AppBarConfig(
                                    title = {
                                        Text(
                                            text = textFieldValue.text,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    },
                                    navigationIcon = R.drawable.ic_arrow_back,
                                    onNavigationButtonClick = {
                                        navController.navigateUp()
                                        if (navController.currentDestination?.route == "search") {
                                            navController.navigateUp()
                                        }
                                        onTextFieldValueChange(TextFieldValue(""))
                                    },
                                    canSearch = true,
                                    searchExpanded = false
                                )
                                route.startsWith("album/") -> AppBarConfig(
                                    onNavigationButtonClick = {
                                        navController.navigateUp()
                                    },
                                    canSearch = false
                                )
                                else -> AppBarConfig()
                            }
                        }
                        val onSearch: (String) -> Unit = { query ->
                            onTextFieldValueChange(TextFieldValue(
                                text = query,
                                selection = TextRange(query.length)
                            ))
                            coroutineScope.launch {
                                SongRepository(this@ComposeActivity).insertSearchHistory(query)
                            }
                            navController.navigate("search/$query")
                        }

                        val scrollBehavior = appBarScrollBehavior(
                            canScroll = { route?.startsWith("search/") == false }
                        )
                        Scaffold(
                            bottomBar = {
                                NavigationBar(
                                    modifier = Modifier
                                        .offset(y = ((NavigationBarHeight.dp + bottomInset) * with(playerBottomSheetState) {
                                            (value - collapsedBound) / (expandedBound - collapsedBound)
                                        }.coerceIn(0f, 1f)))
                                ) {
                                    navigationItems.filterIndexed { index, _ -> enabledNavItems[index] }.forEach { screen ->
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
                            },
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        ) { innerPaddingModifier ->
//                            NavHost(navController, startDestination = navigationItems[defaultNavIndex].route) {
                            NavHost(navController, startDestination = Screen.Songs.route) {
                                composable(Screen.Home.route) {
                                    HomeScreen()
                                }
                                composable(Screen.Songs.route) {
                                    LibrarySongsScreen()
                                }
                                composable(Screen.Artists.route) {
                                    LibraryArtistsScreen(innerPaddingModifier)
                                }
                                composable(Screen.Albums.route) {
                                    LibraryAlbumsScreen(navController, innerPaddingModifier)
                                }
                                composable(Screen.Playlists.route) {
                                    LibraryPlaylistsScreen(innerPaddingModifier)
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
                                ) { backStackEntry ->
                                    AlbumScreen(
                                        albumId = backStackEntry.arguments?.getString("albumId")!!,
                                        playlistId = backStackEntry.arguments?.getString("playlistId"),
                                    )
                                }

                                composable("search") {
                                    SearchScreen(
                                        query = textFieldValue.text,
                                        onTextFieldValueChange = onTextFieldValueChange,
                                        onSearch = onSearch
                                    )
                                }

                                composable(
                                    route = "search/{query}",
                                    arguments = listOf(
                                        navArgument("query") {
                                            type = NavType.StringType
                                        }
                                    )
                                ) { backStackEntry ->
                                    OnlineSearchResult(
                                        query = backStackEntry.arguments?.getString("query")!!,
                                        navController = navController
                                    )
                                }
                            }

                            AppBar(
                                scrollBehavior = scrollBehavior,
                                navController = navController,
                                appBarConfig = appBarConfig,
                                textFieldValue = textFieldValue,
                                onTextFieldValueChange = onTextFieldValueChange,
                                onExpandSearch = {
                                    onTextFieldValueChange(textFieldValue.copy(selection = TextRange(textFieldValue.text.length)))
                                    navController.navigate("search")
                                },
                                onSearch = onSearch
                            )

                            BottomSheetPlayer(playerBottomSheetState)
                        }
                    }
                }
            }
        }
    }
}

val LocalPlayerConnection = staticCompositionLocalOf<PlayerConnection> { error("No PlayerConnection provided") }
val LocalPlayerAwareWindowInsets = compositionLocalOf<WindowInsets> { error("No WindowInsets provided") }