package com.naveenapps.expensemanager.feature.about.licenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.NoSearchResults
import com.naveenapps.expensemanager.core.designsystem.ui.components.SheetSearchField
import com.naveenapps.expensemanager.feature.about.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OpenSourceLicensesScreen(
    viewModel: OpenSourceLicensesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(R.string.licenses),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { viewModel.processAction(OpenSourceLicensesAction.ClosePage) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> CenteredBox { CircularProgressIndicator() }

                state.totalCount == 0 -> CenteredBox {
                    Text(
                        text = stringResource(R.string.licenses_unavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    SheetSearchField(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        query = state.query,
                        onQueryChange = { viewModel.processAction(OpenSourceLicensesAction.Search(it)) },
                        placeholder = stringResource(R.string.search_libraries),
                    )
                    if (state.licenses.isEmpty()) {
                        NoSearchResults(modifier = Modifier.padding(16.dp))
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        items(state.licenses, key = { it.name }) { license ->
                            LicenseRow(
                                license = license,
                                onClick = { viewModel.processAction(OpenSourceLicensesAction.Open(license)) },
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseRow(
    license: OpenSourceLicense,
    onClick: () -> Unit,
) {
    val label = license.kind.label
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Text(text = license.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = if (label != null) {
            { Text(text = label) }
        } else {
            null
        },
    )
}

@Composable
fun OpenSourceLicenseDetailScreen(
    viewModel: OpenSourceLicenseDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = state.name,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = viewModel::closePage,
            )
        },
    ) { innerPadding ->
        val license = state.license
        when {
            state.isLoading -> CenteredBox(Modifier.padding(innerPadding)) { CircularProgressIndicator() }

            license == null -> CenteredBox(Modifier.padding(innerPadding)) {
                Text(
                    text = stringResource(R.string.licenses_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> LicenseContent(license = license, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun LicenseContent(
    license: OpenSourceLicense,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        license.kind.label?.let { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        val url = license.url
        if (url != null) {
            // The library only ships a link to its licence.
            SelectionContainer {
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalButton(onClick = { runCatching { uriHandler.openUri(url) } }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(text = stringResource(R.string.open_license))
            }
        } else {
            SelectionContainer {
                Text(
                    text = license.text,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun CenteredBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
