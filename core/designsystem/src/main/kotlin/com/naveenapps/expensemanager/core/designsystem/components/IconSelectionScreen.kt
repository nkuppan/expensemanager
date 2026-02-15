package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.R
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
    selectedIcon: Int? = null,
    onIconPicked: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        // Header
        Text(
            text = stringResource(id = R.string.choose_icon),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            columns = GridCells.Fixed(5),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(icons, key = { it }) { icon ->
                val isSelected = icon == selectedIcon

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 0.9f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "scale",
                )

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                    animationSpec = tween(200),
                    label = "bg_color",
                )

                val tintColor by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200),
                    label = "tint_color",
                )

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(MaterialTheme.shapes.medium)
                        .background(bgColor)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.shapes.medium,
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onIconPicked.invoke(icon) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            item(span = { GridItemSpan(this.maxLineSpan) }) {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Preview
@Composable
private fun ColorSelectionPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        IconSelectionComponentContent(
            icons = iconSelectionList,
            onIconPicked = {}
        )
    }
}
