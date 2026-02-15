package com.naveenapps.expensemanager.feature.account.reorder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.dragGestureHandler
import com.naveenapps.expensemanager.core.designsystem.components.rememberDragDropListState
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
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
    val state by viewModel.state.collectAsState()

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
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(R.string.accounts_re_order),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    onAction.invoke(AccountReOrderAction.ClosePage)
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
                    onClick = { onAction.invoke(AccountReOrderAction.Save) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                        )
                    },
                    text = {
                        Text(text = stringResource(R.string.save))
                    },
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
                    text = stringResource(R.string.drag_to_reorder_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        itemsIndexed(accounts) { index, account ->
            val isDragging = index == dragDropListState.getCurrentIndexOfDraggedListItem()

            val displacementOffset =
                if (isDragging) {
                    dragDropListState.elementDisplacement.takeIf { it != 0f }
                } else {
                    null
                }

            val elevation by animateDpAsState(
                targetValue = if (isDragging) 8.dp else 0.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "elevation",
            )

            val shape = when {
                accounts.size == 1 -> MaterialTheme.shapes.large
                index == 0 -> RoundedCornerShape(
                    topStart = 14.dp, topEnd = 14.dp,
                    bottomStart = 4.dp, bottomEnd = 4.dp,
                )

                index == accounts.lastIndex -> RoundedCornerShape(
                    topStart = 4.dp, topEnd = 4.dp,
                    bottomStart = 14.dp, bottomEnd = 14.dp,
                )

                else -> RoundedCornerShape(4.dp)
            }

            AppCardView(
                shape = shape,
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
fun AccountReOrderItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    position: Int = 0,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Drag handle
        Icon(
            imageVector = Icons.Rounded.DragHandle,
            contentDescription = null,
            tint = if (isDragging)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp),
        )

        IconAndBackgroundView(
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = name,
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

@AppPreviewsLightAndDarkMode
@Composable
fun AccountReOrderScaffoldViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        AccountReOrderScaffoldView(
            state = AccountReOrderState(
                accounts = getRandomAccountData(5),
                showSaveButton = true,
            ),
            onAction = {},
        )
    }
}
