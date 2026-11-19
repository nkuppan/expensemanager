package com.naveenapps.expensemanager.feature.account.reorder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.components.dragGestureHandler
import com.naveenapps.expensemanager.core.designsystem.components.rememberDragDropListState
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomActionBar
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.feature.account.R
import com.naveenapps.expensemanager.feature.account.list.getRandomAccountData
import kotlinx.coroutines.Job
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountReOrderScreen(
    viewModel: AccountReOrderViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AccountReOrderScaffoldView(
        state = state,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun AccountReOrderScaffoldView(
    state: AccountReOrderState,
    onAction: (AccountReOrderAction) -> Unit,
) {
    val snackbarHostState = remember { EmSnackbarHostState() }

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                title = stringResource(R.string.accounts_re_order),
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
                    onAction.invoke(AccountReOrderAction.ClosePage)
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
                    primaryText = stringResource(R.string.save),
                    onPrimaryClick = { onAction.invoke(AccountReOrderAction.Save) },
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
            // Hint banner
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
                    text = stringResource(R.string.drag_to_reorder_hint),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            }

            ReOrderContent(
                accounts = state.accounts,
                onMove = { from, to ->
                    onAction.invoke(AccountReOrderAction.Swap(from, to))
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ReOrderContent(
    accounts: List<Account>,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val overscrollJob = remember { mutableStateOf<Job?>(null) }
    val dragDropListState = rememberDragDropListState(onMove = onMove)

    LazyColumn(
        modifier = modifier
            .dragGestureHandler(coroutineScope, dragDropListState, overscrollJob),
        state = dragDropListState.getLazyListState(),
        contentPadding = PaddingValues(
            start = EmTheme.spacing.lg,
            end = EmTheme.spacing.lg,
            top = EmTheme.spacing.md,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xxs),
    ) {
        itemsIndexed(accounts) { index, account ->
            val isDragging = index == dragDropListState.getCurrentIndexOfDraggedListItem()

            val displacementOffset =
                if (isDragging) {
                    dragDropListState.elementDisplacement.takeIf { it != 0f }
                } else {
                    null
                }

            EmCard(
                position = EmCardPosition.of(index, accounts),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = displacementOffset ?: 0f
                    },
            ) {
                AccountReOrderItem(
                    name = account.name,
                    icon = account.storedIcon.name,
                    iconBackgroundColor = account.storedIcon.backgroundColor,
                    customImagePath = account.storedIcon.customImagePath,
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
fun AccountReOrderItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    customImagePath: String? = null,
    isDragging: Boolean = false,
    position: Int = 0,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
    ) {
        // Drag handle
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

        IconAndBackgroundView(
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name,
            customImagePath = customImagePath,
        )

        EmText(
            text = name,
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

@EmPreviews
@Composable
fun AccountReOrderScaffoldViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        AccountReOrderScaffoldView(
            state = AccountReOrderState(
                accounts = getRandomAccountData(5),
                showSaveButton = true,
            ),
            onAction = {},
        )
    }
}
