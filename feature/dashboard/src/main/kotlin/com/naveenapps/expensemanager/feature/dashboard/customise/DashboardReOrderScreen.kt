package com.naveenapps.expensemanager.feature.dashboard.customise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.dragGestureHandler
import com.naveenapps.expensemanager.core.designsystem.components.rememberDragDropListState
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
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
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(R.string.customise_dashboard),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { onAction(DashboardReOrderAction.ClosePage) },
                actions = {
                    TextButton(onClick = { onAction(DashboardReOrderAction.Reset) }) {
                        Text(text = stringResource(R.string.customise_reset))
                    }
                },
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = state.showSaveButton,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
                ExtendedFloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { onAction(DashboardReOrderAction.Save) },
                    icon = { Icon(imageVector = Icons.Default.Done, contentDescription = null) },
                    text = { Text(text = stringResource(R.string.customise_save)) },
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
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.customise_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(sections) { index, section ->
            val isDragging = index == dragDropListState.getCurrentIndexOfDraggedListItem()
            val displacementOffset = if (isDragging) {
                dragDropListState.elementDisplacement.takeIf { it != 0f }
            } else {
                null
            }

            AppCardView(
                shape = AppCardViewDefaults.cardShape(index, sections),
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
                        .padding(horizontal = 16.dp, vertical = 14.dp),
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
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.DragHandle,
            contentDescription = null,
            tint = if (isDragging) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier.size(20.dp),
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(section.titleResId),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = "#$position",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
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
    }

private val DashboardSection.icon: ImageVector
    get() = when (this) {
        DashboardSection.SUMMARY -> Icons.Outlined.Insights
        DashboardSection.ACCOUNTS -> Icons.Outlined.AccountBalanceWallet
        DashboardSection.CATEGORIES -> Icons.Outlined.DonutLarge
        DashboardSection.BUDGETS -> Icons.Outlined.Savings
        DashboardSection.TRANSACTIONS -> Icons.AutoMirrored.Outlined.ReceiptLong
        DashboardSection.UPCOMING -> Icons.Outlined.EventRepeat
    }

@AppPreviewsLightAndDarkMode
@Composable
private fun DashboardReOrderPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        DashboardReOrderScaffoldView(
            state = DashboardReOrderState(sections = DashboardSection.DEFAULT_ORDER, showSaveButton = true),
            onAction = {},
        )
    }
}
