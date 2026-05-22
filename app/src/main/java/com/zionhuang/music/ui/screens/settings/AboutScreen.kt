package com.zionhuang.music.ui.screens.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.music.BuildConfig
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.ui.component.IconButton
import com.zionhuang.music.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val uriHandler = LocalUriHandler.current
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val isTablet = configuration.screenWidthDp >= 720

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = if (isTablet) 24.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = spring()) + slideInVertically(initialOffsetY = { it / 4 }, animationSpec = spring())
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable { }
                        .padding(16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.launcher_monochrome),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.height(56.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.spidey_app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = stringResource(R.string.spidey_developer_name),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        AboutCard(title = stringResource(R.string.app_version)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = BuildConfig.VERSION_NAME, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = BuildConfig.FLAVOR.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
                if (BuildConfig.DEBUG) {
                    Text(
                        text = "DEBUG",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.tertiary, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        AboutCard(title = stringResource(R.string.developer_info)) {
            Text(stringResource(R.string.spidey_developer_label), style = MaterialTheme.typography.bodyLarge)
        }

        AboutCard(title = stringResource(R.string.features)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    stringResource(R.string.feature_streaming),
                    stringResource(R.string.feature_material_you),
                    stringResource(R.string.feature_offline_cache),
                    stringResource(R.string.feature_background_playback),
                ).forEach { feature ->
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        AboutCard(title = stringResource(R.string.social_links)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SocialIconButton(R.drawable.discord) { uriHandler.openUri("https://t.me/spideyofficial777") }
                SocialIconButton(R.drawable.share) { uriHandler.openUri("https://t.me/spideyofficial777") }
            }
        }

        AboutCard(title = stringResource(R.string.feedback)) {
            Text(
                text = stringResource(R.string.feedback_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "@spideyofficial777",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        AboutCard(title = stringResource(R.string.credits)) {
            Text(
                text = stringResource(R.string.spidey_credits_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AboutCard(title = stringResource(R.string.device_app_info)) {
            Text(stringResource(R.string.device_name, Build.MODEL ?: "Unknown"), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.android_version, Build.VERSION.RELEASE ?: "Unknown"), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.package_name, context.packageName), style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(8.dp))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.about)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun AboutCard(
    title: String,
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun SocialIconButton(iconRes: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary, BlendMode.SrcIn)
        )
    }
}
