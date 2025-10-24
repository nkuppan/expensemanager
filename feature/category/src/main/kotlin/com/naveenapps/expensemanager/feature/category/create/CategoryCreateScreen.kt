package com.naveenapps.expensemanager.feature.category.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.naveenapps.expensemanager.core.designsystem.components.DeleteDialogItem
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.ui.components.StringTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBarWithDeleteAction
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.category.R

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
            TopNavigationBarWithDeleteAction(
                title = stringResource(id = R.string.category),
                isDeleteEnabled = state.showDeleteButton,
                onNavigationIconClick = { onAction.invoke(CategoryCreateAction.ClosePage) },
                onDeleteActionClick = { onAction.invoke(CategoryCreateAction.ShowDeleteDialog) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction.invoke(CategoryCreateAction.Save) }) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "",
                )
            }
        },
    ) { innerPadding ->
        CategoryCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        CategoryTypeSelectionView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            selectedCategoryType = categoryTypeField.value,
            onCategoryTypeChange = categoryTypeField.onValueChange!!,
        )

        StringTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = nameField.value,
            isError = nameField.valueError,
            onValueChange = nameField.onValueChange,
            label = R.string.category_name,
            errorMessage = stringResource(id = R.string.category_name_error),
        )

        IconAndColorComponent(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            selectedColor = selectedColorField.value,
            selectedIcon = selectedIconField.value,
            onColorSelection = selectedColorField.onValueChange,
            onIconSelection = selectedIconField.onValueChange,
        )
    }
}

@Preview
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

    ExpenseManagerTheme {
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
