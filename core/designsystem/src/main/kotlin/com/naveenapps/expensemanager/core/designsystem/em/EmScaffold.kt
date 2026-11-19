package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

enum class EmTopBarNavigation { None, Back, Close }

enum class EmTopBarSize {
    /** Every pushed screen. */
    Small,

    /** Top-level destinations (Home, Analysis) with a big title. */
    Large,
}

/**
 * The one top bar: background-coloured, title in the screenTitle role, back/close navigation with
 * its content description built in, and [actions] made of EmIconButtons. [titleContent] replaces
 * the title text for a search field in the bar.
 */
@Composable
fun EmTopBar(
    title: String?,
    modifier: Modifier = Modifier,
    navigation: EmTopBarNavigation = EmTopBarNavigation.None,
    onNavigationClick: () -> Unit = {},
    size: EmTopBarSize = EmTopBarSize.Small,
    onTitleClick: (() -> Unit)? = null,
    titleContent: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val (icon, description) = when (navigation) {
        EmTopBarNavigation.None -> null to null
        EmTopBarNavigation.Back -> Icons.AutoMirrored.Filled.ArrowBack to stringResource(R.string.a11y_back)
        EmTopBarNavigation.Close -> Icons.Default.Close to stringResource(R.string.a11y_close)
    }
    EmTopBarImpl(
        title = title,
        modifier = modifier,
        navigationIcon = icon,
        navigationDescription = description,
        onNavigationClick = onNavigationClick,
        size = size,
        onTitleClick = onTitleClick,
        actions = actions,
        titleContent = titleContent,
    )
}

/** Shared by [EmTopBar] and the legacy ExpenseManagerTopAppBar (which takes any icon). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EmTopBarImpl(
    title: String?,
    modifier: Modifier,
    navigationIcon: ImageVector?,
    navigationDescription: String?,
    onNavigationClick: () -> Unit,
    size: EmTopBarSize,
    onTitleClick: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    titleContent: (@Composable () -> Unit)? = null,
) {
    val background = EmTheme.colors.scheme.background
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = background,
        scrolledContainerColor = background,
        titleContentColor = EmTheme.colors.scheme.onBackground,
    )
    val titleSlot: @Composable () -> Unit = titleContent ?: {
        if (!title.isNullOrEmpty()) {
            Text(
                text = title,
                style = if (size == EmTopBarSize.Large) EmTheme.type.display else EmTheme.type.screenTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (onTitleClick != null) Modifier.clickable(onClick = onTitleClick) else Modifier,
            )
        }
    }
    val navigationContent: @Composable () -> Unit = {
        if (navigationIcon != null) {
            EmIconButton(
                icon = navigationIcon,
                contentDescription = navigationDescription ?: stringResource(R.string.a11y_back),
                onClick = onNavigationClick,
            )
        }
    }
    when (size) {
        EmTopBarSize.Small -> TopAppBar(
            title = titleSlot,
            modifier = modifier,
            navigationIcon = navigationContent,
            actions = actions,
            colors = colors,
        )

        EmTopBarSize.Large -> LargeTopAppBar(
            title = titleSlot,
            modifier = modifier,
            navigationIcon = navigationContent,
            actions = actions,
            colors = colors,
        )
    }
}

/**
 * The one screen container: background colour, system-bar insets and a snackbar host, set once.
 * [floatingActionButton] is only for "Add" (EmFab); form saves go in [bottomBar] as an
 * EmBottomActionBar.
 */
@Composable
fun EmScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = EmTheme.colors.scheme.background,
        contentColor = EmTheme.colors.scheme.onBackground,
        content = content,
    )
}
