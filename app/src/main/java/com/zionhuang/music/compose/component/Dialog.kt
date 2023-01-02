package com.zionhuang.music.compose.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DefaultDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    buttons: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.padding(24.dp),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 24.dp)
            ) {
                if (icon != null) {
                    CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.iconContentColor) {
                        Box(
                            Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            icon()
                        }
                    }
                }
                if (title != null) {
                    CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.titleContentColor) {
                        ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                            Box(
                                // Align the title to the center when an icon is present.
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp)
                                    .align(if (icon == null) Alignment.Start else Alignment.CenterHorizontally)
                            ) {
                                title()
                            }
                        }
                    }
                }

                content()

                if (buttons != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 24.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                            ProvideTextStyle(
                                value = MaterialTheme.typography.labelLarge,
                                content = buttons
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ListDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    buttons: (@Composable () -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    DefaultDialog(
        onDismiss = onDismiss,
        modifier = modifier,
        icon = icon,
        title = title,
        buttons = buttons
    ) {
        LazyColumn(content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldDialog(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    initialTextFieldValue: TextFieldValue = TextFieldValue(),
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 10,
    isInputValid: (String) -> Boolean = { it.isNotEmpty() },
    onDone: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val (textFieldValue, onTextFieldValueChange) = remember {
        mutableStateOf(initialTextFieldValue)
    }

    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    DefaultDialog(
        onDismiss = onDismiss,
        modifier = modifier,
        icon = icon,
        title = title,
        buttons = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(android.R.string.cancel))
            }

            TextButton(
                enabled = isInputValid(textFieldValue.text),
                onClick = {
                    onDismiss()
                    onDone(textFieldValue.text)
                }
            ) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    ) {
        TextField(
            value = textFieldValue,
            onValueChange = onTextFieldValueChange,
            placeholder = placeholder,
            singleLine = singleLine,
            maxLines = maxLines,
            colors = TextFieldDefaults.outlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(imeAction = if (singleLine) ImeAction.Done else ImeAction.None),
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone(textFieldValue.text)
                    onDismiss()
                }
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(weight = 1f, fill = false)
                .focusRequester(focusRequester)
        )
    }
}