package com.naveenapps.expensemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmModalSheet
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme

/**
 * In-app explainer shown once, after the first saved transaction, before the system
 * notification-permission dialog. Asking with context (and after the user has seen value)
 * gets far more "Allow" taps than a cold system prompt on first launch.
 */
@Composable
fun ReminderPrimerSheet(
    onTurnOn: () -> Unit,
    onNotNow: () -> Unit,
) {
    EmModalSheet(onDismissRequest = onNotNow) {
        ReminderPrimerContent(onTurnOn = onTurnOn, onNotNow = onNotNow)
    }
}

@Composable
private fun ReminderPrimerContent(
    onTurnOn: () -> Unit,
    onNotNow: () -> Unit,
) {
    // Scrollable so "Turn on" stays reachable in landscape or with a very large font size.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = EmTheme.spacing.xl)
            .padding(bottom = EmTheme.spacing.lg)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(
            modifier = Modifier
                .size(EmTheme.icons.hero)
                .clip(EmTheme.shapes.pill)
                .background(EmTheme.colors.scheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            EmIcon(
                icon = Icons.Outlined.NotificationsActive,
                contentDescription = null,
                tint = EmTheme.colors.scheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(EmTheme.spacing.xl))

        EmText(
            text = stringResource(id = R.string.reminder_primer_title),
            role = EmTextRole.ScreenTitle,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(EmTheme.spacing.sm))

        EmText(
            text = stringResource(id = R.string.reminder_primer_description),
            color = EmTextColor.Supporting,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(EmTheme.spacing.xxl))

        EmButton(
            text = stringResource(id = R.string.reminder_primer_turn_on),
            onClick = onTurnOn,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        )

        Spacer(modifier = Modifier.height(EmTheme.spacing.xs))

        EmButton(
            text = stringResource(id = R.string.reminder_primer_not_now),
            onClick = onNotNow,
            modifier = Modifier.fillMaxWidth(),
            variant = EmButtonVariant.Tertiary,
        )

        EmText(
            text = stringResource(id = R.string.reminder_primer_footnote),
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
            textAlign = TextAlign.Center,
        )
    }
}

@EmPreviews
@Composable
private fun ReminderPrimerContentPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        ReminderPrimerContent(onTurnOn = {}, onNotNow = {})
    }
}
