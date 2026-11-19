package com.naveenapps.expensemanager.core.designsystem.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountKind
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmBadge
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomActionBar
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomSheetContent
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonSize
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmCardVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCheckbox
import com.naveenapps.expensemanager.core.designsystem.em.EmChip
import com.naveenapps.expensemanager.core.designsystem.em.EmChipRow
import com.naveenapps.expensemanager.core.designsystem.em.EmChipVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmConfirmActions
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyAction
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyState
import com.naveenapps.expensemanager.core.designsystem.em.EmFab
import com.naveenapps.expensemanager.core.designsystem.em.EmGroupedList
import com.naveenapps.expensemanager.core.designsystem.em.EmHeaderAction
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmIconTile
import com.naveenapps.expensemanager.core.designsystem.em.EmListItem
import com.naveenapps.expensemanager.core.designsystem.em.EmLoading
import com.naveenapps.expensemanager.core.designsystem.em.EmLoadingVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmMenuItem
import com.naveenapps.expensemanager.core.designsystem.em.EmNavigationBar
import com.naveenapps.expensemanager.core.designsystem.em.EmNavigationBarItem
import com.naveenapps.expensemanager.core.designsystem.em.EmOverflowMenu
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmProgress
import com.naveenapps.expensemanager.core.designsystem.em.EmProgressStatus
import com.naveenapps.expensemanager.core.designsystem.em.EmRadio
import com.naveenapps.expensemanager.core.designsystem.em.EmSectionHeader
import com.naveenapps.expensemanager.core.designsystem.em.EmSegmented
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingItem
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingTrailing
import com.naveenapps.expensemanager.core.designsystem.em.EmSwitch
import com.naveenapps.expensemanager.core.designsystem.em.EmTabs
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextField
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme

/**
 * Every Em* component in its variants, with sample content. The visual catalogue of the design
 * system and the subject of ComponentGalleryScreenshotTest; add a block here with each new
 * component or variant.
 */
@Composable
fun ComponentGallery(
    modifier: Modifier = Modifier,
    // Spinners and indeterminate bars animate forever, so the screen never goes idle and a
    // Robolectric screenshot would wait for ever. Screenshot tests pass false.
    showIndeterminate: Boolean = true,
) {
    Column(
        modifier = modifier.background(EmTheme.colors.scheme.background),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xl),
    ) {
        EmTopBar(title = "Top bar", navigation = EmTopBarNavigation.Back, actions = {
            EmIconButton(icon = Icons.Default.Search, contentDescription = "Search", onClick = {})
        })
        Column(
            modifier = Modifier.padding(horizontal = EmTheme.spacing.screenGutter),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xl),
        ) {
            TextBlock()
            ButtonBlock(showIndeterminate)
            CardBlock()
            ListBlock()
            InputBlock(showIndeterminate)
            StatusBlock(showIndeterminate)
            SheetBlock()
            NavigationBlock()
            EmEmptyState(
                illustration = painterResource(R.drawable.account_balance_wallet),
                title = "No accounts yet",
                body = "Add the accounts you spend from to see balances here.",
                action = EmEmptyAction("Add account") {},
            )
        }
    }
}

@Composable
private fun TextBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs)) {
        EmSectionHeader(title = "Text", action = EmHeaderAction("View all") {})
        EmText(text = "Screen title", role = EmTextRole.ScreenTitle)
        EmText(text = "Section title", role = EmTextRole.SectionTitle)
        EmText(text = "Body text explains something in a sentence or two.")
        EmText(text = "Supporting text", role = EmTextRole.Supporting, color = EmTextColor.Supporting)
        Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg)) {
            EmAmountText(amount = "+₹12,500.00", kind = EmAmountKind.Income, size = EmAmountSize.Large)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg)) {
            EmAmountText(amount = "−₹1,240.50", kind = EmAmountKind.Expense)
            EmAmountText(amount = "₹300.00", kind = EmAmountKind.Transfer)
            EmAmountText(amount = "₹2,000.00", kind = EmAmountKind.Debt)
        }
    }
}

@Composable
private fun ButtonBlock(showIndeterminate: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
        EmSectionHeader(title = "Buttons")
        EmButton(text = "Save", onClick = {}, size = EmButtonSize.Large)
        Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
            EmButton(text = "Primary", onClick = {})
            EmButton(text = "Secondary", onClick = {}, variant = EmButtonVariant.Secondary)
            EmButton(text = "Tertiary", onClick = {}, variant = EmButtonVariant.Tertiary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
            EmButton(text = "Delete", onClick = {}, variant = EmButtonVariant.Destructive)
            EmButton(text = "Add", onClick = {}, leadingIcon = Icons.Default.Add, variant = EmButtonVariant.Secondary)
            if (showIndeterminate) EmButton(text = "Saving", onClick = {}, loading = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
            EmIconButton(icon = Icons.Default.Search, contentDescription = "Search", onClick = {})
            EmIconButton(
                icon = Icons.Default.Add,
                contentDescription = "Add",
                onClick = {},
                variant = EmIconButtonVariant.Tonal,
            )
        }
    }
}

@Composable
private fun CardBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
        EmSectionHeader(title = "Cards")
        EmCardVariant.entries.forEach { variant ->
            EmCard(variant = variant) {
                EmText(
                    text = variant.name,
                    role = EmTextRole.SectionTitle,
                    modifier = Modifier.padding(EmTheme.spacing.lg),
                )
            }
        }
    }
}

@Composable
private fun ListBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
        EmSectionHeader(title = "Lists")
        EmGroupedList(items = listOf(0, 1, 2)) { index ->
            when (index) {
                0 -> EmSettingItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    trailing = EmSettingTrailing.Value("Dark"),
                )

                1 -> EmSettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Daily reminder",
                    supporting = "Every day at 21:00",
                    trailing = EmSettingTrailing.Toggle(checked = true) {},
                )

                else -> EmListItem(
                    title = "Groceries",
                    supporting = "Cash · Today",
                    leading = { EmIconTile(icon = Icons.Default.Add, color = EmTheme.colors.expense) },
                    trailing = { EmAmountText(amount = "−₹640.00", kind = EmAmountKind.Expense) },
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun InputBlock(showIndeterminate: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
        EmSectionHeader(title = "Inputs")
        EmSegmented(
            options = listOf("Income", "Expense", "Transfer"),
            selectedIndex = 1,
            onSelect = {},
            icons = listOf(Icons.Default.Add, Icons.Default.Remove, Icons.Default.SwapHoriz),
            accents = listOf(EmTheme.colors.income, EmTheme.colors.expense, EmTheme.colors.transfer),
        )
        EmChipRow {
            EmChip(label = "Selected", selected = true, onClick = {})
            EmChip(label = "Filter", onClick = {})
            EmChip(label = "Cash", onClick = {}, variant = EmChipVariant.Input, onRemove = {})
            EmChip(label = "Groceries · ₹640", onClick = {}, variant = EmChipVariant.Suggestion)
            if (showIndeterminate) {
                EmChip(label = "Reading…", onClick = {}, variant = EmChipVariant.Suggestion, loading = true)
            }
        }
        EmTextField(value = "", onValueChange = {}, label = "Notes", placeholder = "Optional details")
        EmTextField(value = "abc", onValueChange = {}, label = "Name", isError = true, supportingText = "Required")
        Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
            EmSwitch(checked = true, onCheckedChange = {})
            EmCheckbox(checked = true, onCheckedChange = {})
            EmRadio(selected = true, onClick = {})
        }
    }
}

@Composable
private fun StatusBlock(showIndeterminate: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
        EmSectionHeader(title = "Status")
        Row(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
            EmIconButton(icon = Icons.Default.Notifications, contentDescription = "New", onClick = {}, badge = EmBadge.Dot)
            EmIconButton(icon = Icons.Default.Search, contentDescription = "Filters", onClick = {}, badge = EmBadge.Count(3))
            if (showIndeterminate) EmLoading(variant = EmLoadingVariant.Inline)
            EmFab(icon = Icons.Default.Add, contentDescription = "Add", onClick = {})
        }
        EmProgressStatus.entries.forEachIndexed { index, status ->
            EmProgress(progress = (index + 1) / EmProgressStatus.entries.size.toFloat(), status = status)
        }
        if (showIndeterminate) EmProgress(progress = null)
        EmCard(variant = EmCardVariant.Error) {
            EmText(
                text = "Backup failed",
                role = EmTextRole.SectionTitle,
                modifier = Modifier.padding(EmTheme.spacing.lg),
                color = EmTheme.colors.scheme.onErrorContainer,
            )
        }
        EmBottomActionBar(primaryText = "Save", onPrimaryClick = {}, primaryIcon = Icons.Default.Add)
    }
}

@Composable
private fun SheetBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
        EmSectionHeader(title = "Sheet / confirm")
        EmCard(variant = EmCardVariant.Filled) {
            EmBottomSheetContent(
                title = "Delete account?",
                actions = { EmConfirmActions("Delete", {}, "Cancel", {}, destructive = true) },
            ) {
                EmText(text = "Its 24 transactions are deleted too.", color = EmTextColor.Supporting)
            }
        }
    }
}

@Composable
private fun NavigationBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
        EmSectionHeader(title = "Navigation", onViewAllClick = {})
        EmTabs(tabs = listOf("Expense", "Income"), selectedIndex = 0, onSelect = {})
        Row {
            EmText(text = "Overflow menu", modifier = Modifier.weight(1f))
            EmOverflowMenu(
                contentDescription = "More options",
                items = listOf(EmMenuItem("Rename", {}), EmMenuItem("Delete", {}, destructive = true)),
            )
        }
        EmCard(position = EmCardPosition(0, 2), selected = true) {
            EmListItem(title = "Selected, first in group", onClick = {})
        }
        EmCard(position = EmCardPosition(1, 2)) {
            EmListItem(title = "Second in group", onClick = {})
        }
        EmNavigationBar {
            EmNavigationBarItem(selected = true, onClick = {}, icon = Icons.Default.Palette, label = "Home")
            EmNavigationBarItem(selected = false, onClick = {}, icon = Icons.Default.Search, label = "Transactions")
        }
    }
}

@EmPreviews
@Composable
private fun ComponentGalleryPreview() {
    ExpenseManagerPreviewTheme(padding = EmTheme.spacing.xxs) {
        ComponentGallery()
    }
}
