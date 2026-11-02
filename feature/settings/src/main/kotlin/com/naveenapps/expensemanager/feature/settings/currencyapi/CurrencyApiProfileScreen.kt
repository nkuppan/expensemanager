package com.naveenapps.expensemanager.feature.settings.currencyapi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.model.CurrencyApiPreset
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CurrencyApiProfileScreen(
    viewModel: CurrencyApiProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    CurrencyApiProfileScreenContent(
        state = state,
        onAction = viewModel::processAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyApiProfileScreenContent(
    state: CurrencyApiProfileState,
    onAction: (CurrencyApiProfileAction) -> Unit,
) {
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { onAction.invoke(CurrencyApiProfileAction.ClosePage) },
                title = "Currency API Profile",
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No payment is handled in-app — paste an API key you already " +
                    "generated on the provider's own platform, if the provider requires one.",
            )

            AppCardView {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Provider preset")
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CurrencyApiPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = state.preset == preset,
                                onClick = {
                                    onAction.invoke(CurrencyApiProfileAction.SelectPreset(preset))
                                },
                                label = { Text(preset.name) },
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.baseUrl,
                onValueChange = { onAction.invoke(CurrencyApiProfileAction.ChangeBaseUrl(it)) },
                label = { Text("Base URL") },
                singleLine = true,
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.apiKey,
                onValueChange = { onAction.invoke(CurrencyApiProfileAction.ChangeApiKey(it)) },
                label = { Text("API key (optional)") },
                singleLine = true,
            )

            if (state.errorMessage != null) {
                Text(text = state.errorMessage)
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onAction.invoke(CurrencyApiProfileAction.Save) },
            ) {
                Text("Save")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview
@Composable
private fun CurrencyApiProfileScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CurrencyApiProfileScreenContent(
            state = CurrencyApiProfileState(),
            onAction = {},
        )
    }
}
