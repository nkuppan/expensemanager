package com.naveenapps.expensemanager.core.designsystem.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme

/**
 * Visual catalogue of the design tokens. Also the subject of the screenshot tests, so a change to
 * a token shows up as an image diff. Phase 2 adds a ComponentGallery next to it.
 */
@Composable
fun TokenGallery(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(EmTheme.colors.scheme.background)
            .padding(EmTheme.spacing.screenGutter),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xl),
    ) {
        TypeSection()
        ColorSection()
        SpacingSection()
        ShapeSection()
    }
}

@Composable
private fun GalleryTitle(text: String) {
    Text(text = text.uppercase(), style = EmTheme.type.overline, color = EmTheme.colors.scheme.onSurfaceVariant)
}

@Composable
private fun TypeSection() {
    val type = EmTheme.type
    val roles: List<Pair<String, TextStyle>> = listOf(
        "display" to type.display,
        "screenTitle" to type.screenTitle,
        "sectionTitle" to type.sectionTitle,
        "itemTitle" to type.itemTitle,
        "body" to type.body,
        "supporting" to type.supporting,
        "label" to type.label,
        "overline" to type.overline,
        "amountLarge 1,234.50" to type.amountLarge,
        "amountMedium 1,234.50" to type.amountMedium,
        "amountSmall 1,234.50" to type.amountSmall,
    )
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs)) {
        GalleryTitle("Typography")
        roles.forEach { (name, style) ->
            Text(text = name, style = style, color = EmTheme.colors.scheme.onBackground)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSection() {
    val colors = EmTheme.colors
    val swatches: List<Pair<String, Color>> = listOf(
        "primary" to colors.scheme.primary,
        "income" to colors.income,
        "expense" to colors.expense,
        "transfer" to colors.transfer,
        "debt" to colors.debt,
        "warning" to colors.warning,
        "budgetSafe" to colors.budgetSafe,
        "budgetWatch" to colors.budgetWatch,
        "budgetRisk" to colors.budgetRisk,
        "budgetOver" to colors.budgetOver,
        "surfaceLow" to colors.scheme.surfaceContainerLow,
        "brand" to colors.brandSurface,
    )
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
        GalleryTitle("Colour")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
        ) {
            swatches.forEach { (name, color) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(EmTheme.icons.tile)
                            .background(color, EmTheme.shapes.md),
                    )
                    Text(text = name, style = EmTheme.type.supporting, color = colors.scheme.onBackground)
                }
            }
        }
    }
}

@Composable
private fun SpacingSection() {
    val spacing = EmTheme.spacing
    val steps: List<Pair<String, Dp>> = listOf(
        "xxs" to spacing.xxs,
        "xs" to spacing.xs,
        "sm" to spacing.sm,
        "md" to spacing.md,
        "lg" to spacing.lg,
        "xl" to spacing.xl,
        "xxl" to spacing.xxl,
        "xxxl" to spacing.xxxl,
    )
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs)) {
        GalleryTitle("Spacing")
        steps.forEach { (name, size) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = EmTheme.type.supporting,
                    color = EmTheme.colors.scheme.onBackground,
                    modifier = Modifier.width(spacing.xxxl),
                )
                Box(
                    modifier = Modifier
                        .size(width = size, height = spacing.md)
                        .background(EmTheme.colors.scheme.primary),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShapeSection() {
    val shapes = EmTheme.shapes
    val all: List<Pair<String, Shape>> = listOf(
        "xs" to shapes.xs,
        "sm" to shapes.sm,
        "md" to shapes.md,
        "lg" to shapes.lg,
        "xl" to shapes.xl,
        "pill" to shapes.pill,
    )
    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
        GalleryTitle("Shapes")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
            all.forEach { (name, shape) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(EmTheme.spacing.xxxl)
                            .background(EmTheme.colors.scheme.primaryContainer, shape),
                    )
                    Text(text = name, style = EmTheme.type.supporting, color = EmTheme.colors.scheme.onBackground)
                }
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun TokenGalleryPreview() {
    ExpenseManagerPreviewTheme(padding = EmTheme.spacing.xxs) {
        TokenGallery()
    }
}
