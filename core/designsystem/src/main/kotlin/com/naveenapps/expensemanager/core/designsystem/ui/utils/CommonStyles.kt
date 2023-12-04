package com.naveenapps.expensemanager.core.designsystem.ui.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val COMMON_PADDING = 16

private const val SMALL_ICON_SIZE = 24
private const val ICON_SIZE = 36
private const val COLOR_ICON_SIZE = 72

val SmallIconSpecModifier = Modifier
    .size(SMALL_ICON_SIZE.dp)

val IconSpecModifier = Modifier
    .size(ICON_SIZE.dp)

val ItemSpecModifier = Modifier
    .padding(
        start = COMMON_PADDING.dp,
        end = COMMON_PADDING.dp,
        top = (COMMON_PADDING / 1.25).dp,
        bottom = (COMMON_PADDING / 1.25).dp,
    )
    .wrapContentHeight()
    .fillMaxWidth()

val ColorIconSpecModifier = Modifier
    .height(COLOR_ICON_SIZE.dp)
