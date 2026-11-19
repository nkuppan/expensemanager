package com.naveenapps.expensemanager.feature.debt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

/** A round badge with the person's initial. */
@Composable
fun PersonAvatar(name: String, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(EmTheme.shapes.pill)
            .background(EmTheme.colors.scheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        EmText(
            text = name.trim().firstOrNull()?.uppercase() ?: "?",
            role = EmTextRole.SectionTitle,
            color = EmTheme.colors.scheme.onPrimaryContainer,
        )
    }
}

/** Green when they owe the user, red when the user owes them, muted when settled. */
@Composable
fun DebtStatus.color(): Color = when (this) {
    DebtStatus.OWES_YOU -> EmTheme.colors.income
    DebtStatus.YOU_OWE -> EmTheme.colors.expense
    DebtStatus.SETTLED -> EmTheme.colors.scheme.onSurfaceVariant
}
