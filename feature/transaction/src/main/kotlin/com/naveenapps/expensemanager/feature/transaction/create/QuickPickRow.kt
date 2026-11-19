package com.naveenapps.expensemanager.feature.transaction.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import com.naveenapps.expensemanager.core.designsystem.em.EmChip
import com.naveenapps.expensemanager.core.designsystem.em.EmChipVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.feature.transaction.R

/**
 * One-tap repeats: the user's frequent entries ("Tea · ₹20.00") as a scrolling chip row.
 * Shown under the amount field and on top of the keypad, so it's there whichever opens first.
 */
@Composable
internal fun QuickPickRow(
    picks: List<QuickPick>,
    onPick: (QuickPick) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs),
    ) {
        EmText(
            text = stringResource(R.string.quick_picks),
            modifier = Modifier.padding(
                start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
            ),
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
        )
        LazyRow(
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
        ) {
            items(picks, key = { it.key }) { pick ->
                val name = pick.notes.ifBlank {
                    pick.category.titleResId?.let { stringResource(it) } ?: pick.category.name
                }
                EmChip(
                    label = if (pick.amountText.isBlank()) name else "$name · ${pick.amountText}",
                    onClick = { onPick(pick) },
                    variant = EmChipVariant.Suggestion,
                    leadingIcon = Icons.Outlined.History,
                )
            }
        }
    }
}
