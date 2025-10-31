package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.ui.components.SelectionTitle
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.ColorIconSpecModifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun IconSelectionScreen(
    viewModel: IconSelectionViewModel = koinViewModel(),
    onIconPicked: ((Int) -> Unit)? = null,
) {

    val icons by viewModel.icons.collectAsState()

    IconSelectionComponentContent(
        icons = icons,
        onIconPicked = { selectedIcon ->
            onIconPicked?.invoke(selectedIcon)
        }
    )
}


@Composable
fun IconSelectionComponentContent(
    icons: List<Int>,
    onIconPicked: (Int) -> Unit
) {
    val columns = GridCells.Adaptive(minSize = 48.dp)

    Scaffold {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(it.calculateBottomPadding()),
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
            items(icons, key = { it }) { icon ->
                Box(
                    modifier = ColorIconSpecModifier
                        .aspectRatio(1f)
                        .clickable {
                            onIconPicked.invoke(icon)
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
        }
    }
}

@Preview
@Composable
private fun ColorSelectionPreview() {
    ExpenseManagerTheme {
        IconSelectionComponentContent(
            icons = iconSelectionList,
            onIconPicked = {}
        )
    }
}
