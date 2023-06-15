package com.zionhuang.music.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zionhuang.music.R

@Composable
inline fun <reified T : Enum<T>> SortHeader(
    sortType: T,
    sortDescending: Boolean,
    crossinline onSortTypeChange: (T) -> Unit,
    crossinline onSortDescendingChange: (Boolean) -> Unit,
    crossinline sortTypeText: (T) -> Int,
    trailingText: String,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(sortTypeText(sortType)),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false)
                ) {
                    menuExpanded = !menuExpanded
                }
                .padding(horizontal = 4.dp, vertical = 8.dp)
        )

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.widthIn(min = 172.dp)
        ) {
            enumValues<T>().forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(sortTypeText(type)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(if (sortType == type) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        onSortTypeChange(type)
                        menuExpanded = false
                    }
                )
            }
        }

        ResizableIconButton(
            icon = if (sortDescending) R.drawable.ic_arrow_downward else R.drawable.ic_arrow_upward,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(32.dp)
                .padding(8.dp),
            onClick = { onSortDescendingChange(!sortDescending) }
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = trailingText,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
