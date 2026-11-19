package com.naveenapps.expensemanager.feature.about.licenses

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
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmButtonVariant
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmListItem
import com.naveenapps.expensemanager.core.designsystem.em.EmLoading
import com.naveenapps.expensemanager.core.designsystem.em.EmNoSearchResults
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSearchField
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.feature.about.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OpenSourceLicensesScreen(
    viewModel: OpenSourceLicensesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EmScaffold(
        topBar = {
            EmTopBar(
                title = stringResource(R.string.licenses),
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = { viewModel.processAction(OpenSourceLicensesAction.ClosePage) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> CenteredBox { EmLoading() }

                state.totalCount == 0 -> CenteredBox {
                    EmText(
                        text = stringResource(R.string.licenses_unavailable),
                        color = EmTextColor.Supporting,
                    )
                }

                else -> {
                    EmSearchField(
                        modifier = Modifier.padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm),
                        query = state.query,
                        onQueryChange = { viewModel.processAction(OpenSourceLicensesAction.Search(it)) },
                        placeholder = stringResource(R.string.search_libraries),
                    )
                    if (state.licenses.isEmpty()) {
                        EmNoSearchResults(modifier = Modifier.padding(EmTheme.spacing.lg))
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = EmTheme.spacing.xl),
                    ) {
                        items(state.licenses, key = { it.name }) { license ->
                            LicenseRow(
                                license = license,
                                onClick = { viewModel.processAction(OpenSourceLicensesAction.Open(license)) },
                            )
                            EmDivider(modifier = Modifier.padding(horizontal = EmTheme.spacing.lg))
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
    EmListItem(
        title = license.name,
        supporting = label,
        onClick = onClick,
    )
}

@Composable
fun OpenSourceLicenseDetailScreen(
    viewModel: OpenSourceLicenseDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EmScaffold(
        topBar = {
            EmTopBar(
                title = state.name,
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = viewModel::closePage,
            )
        },
    ) { innerPadding ->
        val license = state.license
        when {
            state.isLoading -> CenteredBox(Modifier.padding(innerPadding)) { EmLoading() }

            license == null -> CenteredBox(Modifier.padding(innerPadding)) {
                EmText(
                    text = stringResource(R.string.licenses_unavailable),
                    color = EmTextColor.Supporting,
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
            .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
    ) {
        license.kind.label?.let { label ->
            EmText(
                text = label,
                role = EmTextRole.ItemTitle,
                color = EmTextColor.Primary,
            )
        }
        val url = license.url
        if (url != null) {
            // The library only ships a link to its licence.
            SelectionContainer {
                EmText(
                    text = url,
                    color = EmTextColor.Supporting,
                )
            }
            EmButton(
                text = stringResource(R.string.open_license),
                onClick = { runCatching { uriHandler.openUri(url) } },
                variant = EmButtonVariant.Secondary,
                leadingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
            )
        } else {
            SelectionContainer {
                EmText(text = license.text, role = EmTextRole.Code, modifier = Modifier.fillMaxWidth())
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
            .padding(EmTheme.spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
