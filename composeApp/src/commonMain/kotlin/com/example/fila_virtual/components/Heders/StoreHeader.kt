package com.example.fila_virtual.components.Heders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.ExtraLightGray
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.general_back
import fila_virtual.composeapp.generated.resources.store_closed_now
import fila_virtual.composeapp.generated.resources.store_open_now
import fila_virtual.composeapp.generated.resources.user_cart_open
import org.jetbrains.compose.resources.stringResource

@Composable
fun StoreHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isOpen: Boolean? = null,
    cartCount: Int? = null,
    onCartClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    backButtonContainerColor: Color? = ExtraLightGray,
    titleStyle: TextStyle? = null,
    titleColor: Color = DarkGray,
    backContentDescription: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
    centerTitle: Boolean = true,
    includeStatusBarPadding: Boolean = true,
    minHeight: Dp = 72.dp,
    horizontalPadding: Dp = 12.dp,
    verticalPadding: Dp = 8.dp
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor ?: MaterialTheme.colorScheme.background
    ) {
        val statusBarModifier = if (includeStatusBarPadding) Modifier.statusBarsPadding() else Modifier
        val hasTrailingAction = (cartCount != null && onCartClick != null) || trailingContent != null
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(statusBarModifier)
                .heightIn(min = minHeight)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(backButtonContainerColor ?: Color.Transparent, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = backContentDescription ?: stringResource(Res.string.general_back),
                        tint = DarkGray
                    )
                }
            } else if (centerTitle && hasTrailingAction) {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = if (centerTitle) Alignment.CenterHorizontally else Alignment.Start
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = titleStyle ?: MaterialTheme.typography.titleMedium,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MediumGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = if (centerTitle) androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start
                    )
                }
                isOpen?.let { open ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = if (open) Color(0xFF41B883) else MediumGray
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(if (open) Res.string.store_open_now else Res.string.store_closed_now),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (open) Color(0xFF23966B) else MediumGray,
                            maxLines = 1
                        )
                    }
                }
            }

            if (cartCount != null && onCartClick != null) {
                CartActionButton(
                    cartCount = cartCount,
                    onClick = onCartClick
                )
            } else if (trailingContent != null) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    trailingContent()
                }
            } else if (onBack != null || (centerTitle && hasTrailingAction)) {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
fun CartActionButton(
    cartCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = ExtraLightGray
) {
    val haptic = LocalHapticFeedback.current
    BadgedBox(
        badge = {
            if (cartCount > 0) {
                Badge(containerColor = PrimaryOrange, contentColor = Color.White) {
                    Text(cartCount.toString())
                }
            }
        }
    ) {
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
            modifier = modifier.size(48.dp).background(containerColor, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Outlined.ShoppingCart,
                contentDescription = stringResource(Res.string.user_cart_open),
                tint = DarkGray
            )
        }
    }
}