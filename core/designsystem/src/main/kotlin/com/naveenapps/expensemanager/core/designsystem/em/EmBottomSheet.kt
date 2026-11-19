package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

/**
 * The one bottom sheet: optional title, a scrolling body and a sticky [actions] row that always
 * stays on screen (the hidden-Apply-button bug can't happen). Body padding is set here.
 */
@Composable
fun EmBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    EmModalSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
        EmBottomSheetContent(title = title, actions = actions, content = content)
    }
}

/** Sheet layout without the modal, for previews and screenshot tests. */
@Composable
internal fun EmBottomSheetContent(
    title: String?,
    actions: (@Composable RowScope.() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = EmTheme.spacing
    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        if (title != null) {
            EmText(
                text = title,
                role = EmTextRole.ScreenTitle,
                modifier = Modifier.padding(horizontal = spacing.xl, vertical = spacing.sm),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.xl, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
            content = content,
        )
        if (actions != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xl, vertical = spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(spacing.md, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}

/**
 * The one confirmation: a bottom sheet with a message, Cancel and a confirm button. [destructive]
 * paints the confirm button in the error colour (deleting, discarding).
 */
@Composable
fun EmConfirm(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    dismissText: String = stringResource(R.string.cancel),
    destructive: Boolean = false,
) {
    EmBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = title,
        actions = { EmConfirmActions(confirmText, onConfirm, dismissText, onDismiss, destructive) },
    ) {
        if (message != null) {
            EmText(text = message, color = EmTextColor.Supporting)
        }
    }
}

@Composable
internal fun RowScope.EmConfirmActions(
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismiss: () -> Unit,
    destructive: Boolean,
) {
    EmButton(
        text = dismissText,
        onClick = onDismiss,
        variant = EmButtonVariant.Secondary,
        size = EmButtonSize.Medium,
        modifier = Modifier.weight(1f),
    )
    EmButton(
        text = confirmText,
        onClick = onConfirm,
        variant = if (destructive) EmButtonVariant.Destructive else EmButtonVariant.Primary,
        size = EmButtonSize.Medium,
        modifier = Modifier.weight(1f),
    )
}

/** "Delete?" confirmation with Keep / Delete buttons. [message] defaults to the generic one. */
@Composable
fun EmDeleteConfirm(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    message: String? = null,
) {
    EmConfirm(
        title = stringResource(R.string.delete),
        message = message ?: stringResource(R.string.delete_item_message),
        confirmText = stringResource(R.string.yes_delete),
        dismissText = stringResource(R.string.no_keep),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        destructive = true,
    )
}
