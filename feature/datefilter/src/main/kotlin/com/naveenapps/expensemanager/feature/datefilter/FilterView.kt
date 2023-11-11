package com.naveenapps.expensemanager.feature.datefilter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterView(modifier: Modifier = Modifier) {

    val viewModel: FilterViewModel = hiltViewModel()

    val date by viewModel.date.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp)
        ) {
            DateFilterSelectionView {
                showBottomSheet = false
            }
        }
    }

    FilterContentView(modifier, date) {
        showBottomSheet = true
    }
}

@Composable
private fun FilterContentView(
    modifier: Modifier,
    date: String,
    showBottomSheet: () -> Unit
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
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = null
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
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
        FilterContentView(modifier = Modifier.fillMaxWidth(), date = "This Month (11/2023)") {

        }
    }
}