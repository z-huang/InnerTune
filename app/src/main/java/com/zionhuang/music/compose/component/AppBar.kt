package com.zionhuang.music.compose.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
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

@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    navController: NavController,
    appBarState: AppBarState,
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onExpandSearch: () -> Unit,
    onSearch: (String) -> Unit,
) {
    val topInset = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val transition = updateTransition(targetState = appBarState.canSearch && !appBarState.searchExpanded, "canSearch")
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

    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(appBarState.searchExpanded) {
        if (appBarState.searchExpanded) {
            delay(300)
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(topInset)
            .background(MaterialTheme.colorScheme
                .surfaceColorAtElevation(6.dp)
                .copy(alpha = percent))
    )

    Box(
        modifier = Modifier
            .padding(WindowInsets.systemBars
                .only(WindowInsetsSides.Top + WindowInsetsSides.End)
                .asPaddingValues())
            .fillMaxWidth()
            .height(AppBarHeight.dp)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .clip(RoundedCornerShape(cornerShapePercent))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp))
            .clickable {
                if (appBarState.canSearch) {
                    onExpandSearch()
                }
            }
    ) {
        AnimatedVisibility(
            visible = appBarState.canSearch,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AnimatedVisibility(
                visible = !appBarState.searchExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        onClick = appBarState.onNavigationButtonClick
                    ) {
                        Icon(
                            painter = painterResource(appBarState.navigationIcon),
                            contentDescription = null
                        )
                    }

                    if (appBarState.title == null) {
                        Text(
                            text = "Search",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .alpha(0.6f)
                                .weight(1f)
                        )
                    } else {
                        Text(
                            text = appBarState.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = appBarState.searchExpanded,
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
                            painter = painterResource(appBarState.navigationIcon),
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
                                        text = "Search",
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
                            modifier = Modifier.padding(horizontal = 4.dp),
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
                }
            }
        }

        AnimatedVisibility(
            visible = !appBarState.canSearch,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    onClick = appBarState.onNavigationButtonClick
                ) {
                    Icon(
                        painter = painterResource(appBarState.navigationIcon),
                        contentDescription = null
                    )
                }

                Text(
                    text = appBarState.title.orEmpty(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                )
            }
        }
    }
}

@Immutable
data class AppBarState(
    val title: String? = null,
    @DrawableRes val navigationIcon: Int = R.drawable.ic_arrow_back,
    val onNavigationButtonClick: () -> Unit = {},
    val canSearch: Boolean = true,
    val searchExpanded: Boolean = false,
    val actions: @Composable RowScope.() -> Unit = {},
    val transparentBackground: Boolean = false,
)