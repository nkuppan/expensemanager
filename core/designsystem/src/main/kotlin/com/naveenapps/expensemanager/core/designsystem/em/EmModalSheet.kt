package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

/**
 * A bare modal bottom sheet (always fully expanded, no layout inside). Most sheets use
 * [EmBottomSheet], which adds the title, scrolling body and sticky actions; use this only for
 * sheets that lay out their own lists (pickers with a LazyColumn).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmModalSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // SheetState is fully internal — callers cannot misuse it
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    var isLayoutReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLayoutReady = true
    }

    if (!isLayoutReady) return

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = EmTheme.shapes.sheet,
        content = content,
        containerColor = EmTheme.colors.scheme.surfaceContainerLowest,
        contentColor = EmTheme.colors.scheme.onSurface,
    )
}
