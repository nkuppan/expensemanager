package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.components.SelectionTitle
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ColorIconSpecModifier

@Composable
fun IconSelectionScreen(
    viewModel: IconSelectionViewModel = hiltViewModel(),
    onIconPicked: ((Int) -> Unit)? = null,
) {
    val columns = GridCells.Adaptive(minSize = 48.dp)

    val icons by viewModel.icons.collectAsState()

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        columns = columns,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(span = {
            GridItemSpan(this.maxLineSpan)
        }) {
            SelectionTitle(
                stringResource(id = R.string.choose_icon),
                Modifier.Companion
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
            )
        }
        items(icons) { icon ->
            Box(
                modifier = ColorIconSpecModifier
                    .aspectRatio(1f)
                    .clickable {
                        onIconPicked?.invoke(icon)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    modifier = Modifier.align(Alignment.Center),
                    contentDescription = null,
                )
            }
        }
        item(span = {
            GridItemSpan(this.maxLineSpan)
        }) {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Preview
@Composable
private fun ColorSelectionPreview() {
    ExpenseManagerTheme {
        IconSelectionScreen()
    }
}
