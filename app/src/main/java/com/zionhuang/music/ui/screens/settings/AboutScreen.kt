package com.zionhuang.music.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.music.BuildConfig
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_monochrome),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation))
                .clickable { }
        )

        Text(
            text = "InnerTune",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = BuildConfig.VERSION_NAME,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            if (BuildConfig.DEBUG) {
                Spacer(Modifier.width(4.dp))

                Text(
                    text = "DEBUG",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                        .padding(
                            horizontal = 4.dp,
                            vertical = 2.dp
                        )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row {
            IconButton(
                onClick = { uriHandler.openUri("https://github.com/z-huang/InnerTune") }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_github),
                    contentDescription = null
                )
            }

            IconButton(
                onClick = { uriHandler.openUri("https://liberapay.com/zionhuang") }
            ) {
                Icon(
                    painter = painterResource(R.drawable.liberapay),
                    contentDescription = null
                )
            }

            IconButton(
                onClick = { uriHandler.openUri("https://www.buymeacoffee.com/zionhuang") }
            ) {
                Icon(
                    painter = painterResource(R.drawable.buymeacoffee),
                    contentDescription = null
                )
            }
        }

    }

    TopAppBar(
        title = { Text(stringResource(R.string.about)) },
        navigationIcon = {
            IconButton(onClick = navController::navigateUp) {
                Icon(
                    painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
