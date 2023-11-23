package com.naveenapps.expensemanager.feature.datefilter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterView(modifier: Modifier = Modifier) {

    val viewModel: FilterViewModel = hiltViewModel()

    val date by viewModel.date.collectAsState()
    val showForward by viewModel.showForward.collectAsState()
    val showBackward by viewModel.showBackward.collectAsState()

    var showDateFilter by remember { mutableStateOf(false) }
    var showAllFilter by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDateFilter) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    showDateFilter = false
                    bottomSheetState.hide()
                }
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp)
        ) {
            DateFilterSelectionView {
                scope.launch {
                    showDateFilter = false
                    bottomSheetState.hide()
                }
            }
        }
    }

    if (showAllFilter) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    showAllFilter = false
                    bottomSheetState.hide()
                }
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp)
        ) {
            DateFilterSelectionView {
                scope.launch {
                    showAllFilter = false
                    bottomSheetState.hide()
                }
            }
        }
    }

    FilterContentView(
        modifier = modifier,
        showForward = showForward,
        showBackward = showBackward,
        date = date,
        showBottomSheet = {
            showDateFilter = true
        },
        onForwardClick = viewModel::moveDateRangeForward,
        onBackwardClick = viewModel::moveDateRangeBackward,
        onFilterClick = {
            showAllFilter = true
        }
    )
}

@Composable
private fun FilterContentView(
    showBackward: Boolean,
    showForward: Boolean,
    date: String,
    showBottomSheet: () -> Unit,
    onForwardClick: () -> Unit,
    onBackwardClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Row(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .height(40.dp)
                .clickable {
                    showBottomSheet.invoke()
                }
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 16.dp),
                imageVector = Icons.Default.EditCalendar,
                contentDescription = null
            )
            Text(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically),
                text = date,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(
            onClick = onBackwardClick,
            enabled = showBackward
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null
            )
        }
        IconButton(
            onClick = onForwardClick,
            enabled = showForward
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
        IconButton(onClick = onFilterClick) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
fun FilterViewPreview() {
    ExpenseManagerTheme {
        Column {
            FilterContentView(
                showBackward = false,
                showForward = false,
                date = "This Month (11/2023)",
                showBottomSheet = {},
                onForwardClick = {},
                onBackwardClick = {},
                onFilterClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            FilterContentView(
                showBackward = true,
                showForward = true,
                date = "This Month (11/2023)",
                showBottomSheet = {},
                onForwardClick = {},
                onBackwardClick = {},
                onFilterClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}