package com.nkuppan.expensemanager.common.ui.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val COMMON_PADDING = 16

private const val ICON_SIZE = 28
private const val COLOR_ICON_SIZE = 72

val IconSpecModifier = Modifier
    .padding(end = COMMON_PADDING.dp)
    .size(ICON_SIZE.dp)

val ItemSpecModifier = Modifier
    .padding(COMMON_PADDING.dp)
    .wrapContentHeight()
    .fillMaxWidth()

val ColorIconSpecModifier = Modifier
    .height(COLOR_ICON_SIZE.dp)