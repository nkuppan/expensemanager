package com.naveenapps.expensemanager.feature.dashboard.customise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.components.dragGestureHandler
import com.naveenapps.expensemanager.core.designsystem.components.rememberDragDropListState
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomActionBar
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.model.DashboardSection
import com.naveenapps.expensemanager.feature.dashboard.R
import kotlinx.coroutines.Job
import org.koin.compose.viewmodel.koinViewModel

/** Customise dashboard: hold and drag Home's sections, like Accounts → Re-order, then Save. */
@Composable
fun DashboardReOrderScreen(
    viewModel: DashboardReOrderViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DashboardReOrderScaffoldView(state = state, onAction = viewModel::processAction)
}

@Composable
private fun DashboardReOrderScaffoldView(
    state: DashboardReOrderState,
    onAction: (DashboardReOrderAction) -> Unit,
) {
    EmScaffold(
        topBar = {
            EmTopBar(
                title = stringResource(R.string.customise_dashboard),
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = { onAction(DashboardReOrderAction.ClosePage) },
                actions = {
                    EmButton(
                        text = stringResource(R.string.customise_reset),
                        onClick = { onAction(DashboardReOrderAction.Reset) },
                        variant = EmButtonVariant.Tertiary,
                    )
                },
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = state.showSaveButton,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                EmBottomActionBar(
                    primaryText = stringResource(R.string.customise_save),
                    onPrimaryClick = { onAction(DashboardReOrderAction.Save) },
                    primaryIcon = Icons.Default.Done,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = EmTheme.spacing.xl, vertical = EmTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            ) {
                EmIcon(
                    icon = Icons.Rounded.Info,
                    contentDescription = null,
                    size = EmTheme.icons.sm,
                    tint = EmTheme.colors.scheme.onSurfaceVariant,
                )
                EmText(
                    text = stringResource(R.string.customise_hint),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            }

            ReOrderContent(
                sections = state.sections,
                onMove = { from, to -> onAction(DashboardReOrderAction.Swap(from, to)) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ReOrderContent(
    sections: List<DashboardSection>,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val overscrollJob = remember { mutableStateOf<Job?>(null) }
    val dragDropListState = rememberDragDropListState(onMove = onMove)

    LazyColumn(
        modifier = modifier.dragGestureHandler(coroutineScope, dragDropListState, overscrollJob),
        state = dragDropListState.getLazyListState(),
        contentPadding = PaddingValues(
            start = EmTheme.spacing.lg,
            end = EmTheme.spacing.lg,
            top = EmTheme.spacing.md,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xxs),
    ) {
        itemsIndexed(sections) { index, section ->
            val isDragging = index == dragDropListState.getCurrentIndexOfDraggedListItem()
            val displacementOffset = if (isDragging) {
                dragDropListState.elementDisplacement.takeIf { it != 0f }
            } else {
                null
            }

            EmCard(
                position = EmCardPosition.of(index, sections),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = displacementOffset ?: 0f },
            ) {
                DashboardSectionItem(
                    section = section,
                    isDragging = isDragging,
                    position = index + 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
                )
            }
        }
    }
}

@Composable
private fun DashboardSectionItem(
    section: DashboardSection,
    isDragging: Boolean,
    position: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
    ) {
        EmIcon(
            icon = Icons.Rounded.DragHandle,
            contentDescription = null,
            size = EmTheme.icons.md,
            tint = if (isDragging) {
                EmTheme.colors.scheme.primary
            } else {
                EmTheme.colors.scheme.onSurfaceVariant
            },
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(EmTheme.shapes.md)
                .background(EmTheme.colors.scheme.primary.tint()),
            contentAlignment = Alignment.Center,
        ) {
            EmIcon(
                icon = section.icon,
                contentDescription = null,
                tint = EmTheme.colors.scheme.primary,
            )
        }

        EmText(
            text = stringResource(section.titleResId),
            modifier = Modifier.weight(1f),
            role = EmTextRole.ItemTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        EmText(
            text = "#$position",
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
        )
    }
}

private val DashboardSection.titleResId: Int
    get() = when (this) {
        DashboardSection.SUMMARY -> R.string.section_summary
        DashboardSection.ACCOUNTS -> R.string.section_accounts
        DashboardSection.CATEGORIES -> R.string.section_categories
        DashboardSection.BUDGETS -> R.string.section_budgets
        DashboardSection.TRANSACTIONS -> R.string.section_transactions
        DashboardSection.UPCOMING -> R.string.section_upcoming
        DashboardSection.DEBTS -> R.string.home_debts_title
    }

private val DashboardSection.icon: ImageVector
    get() = when (this) {
        DashboardSection.SUMMARY -> Icons.Outlined.Insights
        DashboardSection.ACCOUNTS -> Icons.Outlined.AccountBalanceWallet
        DashboardSection.CATEGORIES -> Icons.Outlined.DonutLarge
        DashboardSection.BUDGETS -> Icons.Outlined.Savings
        DashboardSection.TRANSACTIONS -> Icons.AutoMirrored.Outlined.ReceiptLong
        DashboardSection.UPCOMING -> Icons.Outlined.EventRepeat
        DashboardSection.DEBTS -> Icons.Outlined.People
    }

@EmPreviews
@Composable
private fun DashboardReOrderPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        DashboardReOrderScaffoldView(
            state = DashboardReOrderState(sections = DashboardSection.DEFAULT_ORDER, showSaveButton = true),
            onAction = {},
        )
    }
}
