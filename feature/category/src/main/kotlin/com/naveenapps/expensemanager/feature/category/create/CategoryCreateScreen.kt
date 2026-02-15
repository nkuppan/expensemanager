package com.naveenapps.expensemanager.feature.category.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.DeleteDialogItem
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.StringTextField
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.category.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryCreateScreen(
    viewModel: CategoryCreateViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    CategoryCreateScreenContentView(
        state = state,
        onAction = viewModel::processAction
    )
}

@Composable
private fun CategoryCreateScreenContentView(
    state: CategoryCreateState,
    onAction: (CategoryCreateAction) -> Unit,
) {

    val snackbarHostState = remember { SnackbarHostState() }

    if (state.showDeleteDialog) {
        DeleteDialogItem(
            confirm = { onAction.invoke(CategoryCreateAction.Delete) },
            dismiss = { onAction.invoke(CategoryCreateAction.DismissDeleteDialog) }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    onAction.invoke(CategoryCreateAction.ClosePage)
                },
                title = if (state.showDeleteButton)
                    stringResource(R.string.edit_category)
                else
                    stringResource(R.string.create_category),
                actions = {
                    if (state.showDeleteButton) {
                        IconButton(onClick = { onAction.invoke(CategoryCreateAction.ShowDeleteDialog) }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction.invoke(CategoryCreateAction.Save) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                    )
                },
                text = {
                    Text(text = stringResource(R.string.save))
                },
            )
        },
    ) { innerPadding ->
        CategoryCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            nameField = state.name,
            categoryTypeField = state.type,
            selectedColorField = state.color,
            selectedIconField = state.icon,
        )
    }
}

@Composable
private fun CategoryCreateScreen(
    nameField: TextFieldValue<String>,
    categoryTypeField: TextFieldValue<CategoryType>,
    selectedColorField: TextFieldValue<String>,
    selectedIconField: TextFieldValue<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        // Category type section
        SectionHeader(
            title = stringResource(R.string.category_type),
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
        )
        CategoryTypeSelectionView(
            modifier = Modifier.fillMaxWidth(),
            selectedCategoryType = categoryTypeField.value,
            onCategoryTypeChange = categoryTypeField.onValueChange!!,
        )

        // Name section
        SectionHeader(
            title = stringResource(R.string.details),
            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
        )
        StringTextField(
            modifier = Modifier.fillMaxWidth(),
            value = nameField.value,
            isError = nameField.valueError,
            onValueChange = nameField.onValueChange,
            label = R.string.category_name,
            errorMessage = stringResource(id = R.string.category_name_error),
        )

        // Icon and color section
        SectionHeader(
            title = stringResource(R.string.appearance),
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
        )
        IconAndColorComponent(
            modifier = Modifier.fillMaxWidth(),
            selectedColor = selectedColorField.value,
            selectedIcon = selectedIconField.value,
            onColorSelection = selectedColorField.onValueChange,
            onIconSelection = selectedIconField.onValueChange,
        )

        // Bottom spacer for FAB clearance
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}

@AppPreviewsLightAndDarkMode
@Composable
private fun CategoryCreateStatePreview() {
    val nameField = TextFieldValue(
        value = "", valueError = false, onValueChange = { }
    )
    val selectedColorField = TextFieldValue(
        value = "#000000", valueError = false, onValueChange = { }
    )
    val selectedIconField = TextFieldValue(
        value = "account_balance_wallet", valueError = false, onValueChange = { }
    )
    val categoryType = TextFieldValue(
        value = CategoryType.EXPENSE, valueError = false, onValueChange = { }
    )

    NaveenAppsPreviewTheme(padding = 0.dp) {
        CategoryCreateScreenContentView(
            state = CategoryCreateState(
                name = nameField,
                type = categoryType,
                color = selectedColorField,
                icon = selectedIconField,
                showDeleteButton = false,
                showDeleteDialog = false
            ),
            onAction = {}
        )
    }
}
