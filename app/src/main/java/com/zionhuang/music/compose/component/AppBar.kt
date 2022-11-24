package com.zionhuang.music.compose.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zionhuang.music.R
import com.zionhuang.music.constants.AppBarHeight
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    navController: NavController,
    appBarConfig: AppBarConfig,
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onExpandSearch: () -> Unit,
    onSearch: (String) -> Unit,
) {
    val topInset = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val transition = updateTransition(targetState = appBarConfig.canSearch && !appBarConfig.searchExpanded, "canSearch")
    val horizontalPadding by transition.animateDp(label = "") {
        if (it) 12.dp else 0.dp
    }
    val verticalPadding by transition.animateDp(label = "") {
        if (it) 8.dp else 0.dp
    }
    val cornerShapePercent by transition.animateInt(label = "") {
        if (it) 50 else 0
    }
    val percent by transition.animateFloat(label = "") {
        if (it) 0f else 1f
    }
    val background by animateColorAsState(if (appBarConfig.canSearch) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
    } else {
        MaterialTheme.colorScheme.background
    })

    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(appBarConfig.searchExpanded) {
        if (appBarConfig.searchExpanded) {
            delay(300)
            focusRequester.requestFocus()
        }
    }

    val heightOffsetLimit = with(LocalDensity.current) { -(AppBarHeight.dp + topInset).toPx() }
    SideEffect {
        if (scrollBehavior.state.heightOffsetLimit != heightOffsetLimit) {
            scrollBehavior.state.heightOffsetLimit = heightOffsetLimit
        }
    }

    Box(
        modifier = Modifier.offset(
            y = with(LocalDensity.current) {
                scrollBehavior.state.heightOffset.roundToInt().toDp()
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topInset)
                .background(if (appBarConfig.canSearch) {
                    MaterialTheme.colorScheme
                        .surfaceColorAtElevation(6.dp)
                        .copy(alpha = percent)
                } else {
                    MaterialTheme.colorScheme.background
                })
        )

        Box(
            modifier = Modifier
                .padding(WindowInsets.systemBars
                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                    .asPaddingValues())
                .fillMaxWidth()
                .height(AppBarHeight.dp)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .clip(RoundedCornerShape(cornerShapePercent))
                .background(background)
                .clickable(
                    indication = if (appBarConfig.canSearch && !appBarConfig.searchExpanded) LocalIndication.current else null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (appBarConfig.canSearch && !appBarConfig.searchExpanded) {
                        scrollBehavior.state.heightOffset = 0f
                        onExpandSearch()
                    }
                }
        ) {
            AnimatedVisibility(
                visible = appBarConfig.canSearch,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AnimatedVisibility(
                    visible = !appBarConfig.searchExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        IconButton(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            onClick = appBarConfig.onNavigationButtonClick
                        ) {
                            Icon(
                                painter = painterResource(appBarConfig.navigationIcon),
                                contentDescription = null
                            )
                        }

                        appBarConfig.title(this)
                    }
                }

                AnimatedVisibility(
                    visible = appBarConfig.searchExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        IconButton(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            onClick = {
                                navController.navigateUp()
                            }
                        ) {
                            Icon(
                                painter = painterResource(appBarConfig.navigationIcon),
                                contentDescription = null
                            )
                        }

                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = onTextFieldValueChange,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                            singleLine = true,
                            maxLines = 1,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    onSearch(textFieldValue.text)
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxHeight(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (textFieldValue.text.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.menu_search),
                                            textAlign = TextAlign.Start,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .alpha(0.6f)
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                        )

                        if (textFieldValue.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onTextFieldValueChange(TextFieldValue(""))
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = null
                                )
                            }
                        }

                        appBarConfig.actions(this)
                    }
                }
            }

            AnimatedVisibility(
                visible = !appBarConfig.canSearch,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        onClick = appBarConfig.onNavigationButtonClick
                    ) {
                        Icon(
                            painter = painterResource(appBarConfig.navigationIcon),
                            contentDescription = null
                        )
                    }

                    appBarConfig.title(this)

                    appBarConfig.actions(this)
                }
            }
        }
    }
}

@Immutable
data class AppBarConfig(
    val title: @Composable RowScope.() -> Unit = {},
    @DrawableRes val navigationIcon: Int = R.drawable.ic_arrow_back,
    val onNavigationButtonClick: () -> Unit = {},
    val canSearch: Boolean = true,
    val searchExpanded: Boolean = false,
    val actions: @Composable RowScope.() -> Unit = {},
    val transparentBackground: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appBarScrollBehavior(
    state: TopAppBarState = rememberTopAppBarState(),
    canScroll: () -> Boolean = { true },
    snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay(),
): TopAppBarScrollBehavior =
    AppBarScrollBehavior(
        state = state,
        snapAnimationSpec = snapAnimationSpec,
        flingAnimationSpec = flingAnimationSpec,
        canScroll = canScroll
    )

@ExperimentalMaterial3Api
class AppBarScrollBehavior constructor(
    override val state: TopAppBarState,
    override val snapAnimationSpec: AnimationSpec<Float>?,
    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
    val canScroll: () -> Boolean = { true },
) : TopAppBarScrollBehavior {
    override val isPinned: Boolean = false
    override var nestedScrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (!canScroll()) return Offset.Zero
            state.contentOffset += consumed.y
            if (state.heightOffset == 0f || state.heightOffset == state.heightOffsetLimit) {
                if (consumed.y == 0f && available.y > 0f) {
                    // Reset the total content offset to zero when scrolling all the way down.
                    // This will eliminate some float precision inaccuracies.
                    state.contentOffset = 0f
                }
            }
            state.heightOffset += consumed.y
            return Offset.Zero
        }
    }
}
