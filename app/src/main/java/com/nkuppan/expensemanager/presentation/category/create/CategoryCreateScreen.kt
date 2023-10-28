package com.nkuppan.expensemanager.presentation.category.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.presentation.selection.ColorSelectionScreen
import com.nkuppan.expensemanager.presentation.selection.IconAndColorComponent
import com.nkuppan.expensemanager.presentation.selection.IconSelectionScreen
import com.nkuppan.expensemanager.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.ui.theme.widget.AppDialog
import com.nkuppan.expensemanager.ui.theme.widget.TopNavigationBarWithDeleteAction
import com.nkuppan.expensemanager.ui.utils.UiText
import kotlinx.coroutines.launch


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryCreateScreen(
    navController: NavController,
    categoryId: String?,
) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: CategoryCreateViewModel = hiltViewModel()

    var sheetSelection by remember { mutableIntStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AppDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            onConfirmation = {
                viewModel.deleteCategory()
                showDeleteDialog = false
            },
            dialogTitle = stringResource(id = R.string.delete),
            dialogText = stringResource(id = R.string.delete_item_message),
            positiveButtonText = stringResource(id = R.string.delete),
            negativeButtonText = stringResource(id = R.string.cancel)
        )
    }

    val categoryCreated by viewModel.categoryUpdated.collectAsState(false)
    if (categoryCreated) {
        LaunchedEffect(key1 = "completed", block = {
            navController.popBackStack()
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.category_create_success)
            )
        })
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = bottomSheetState,
            windowInsets = WindowInsets(0.dp)
        ) {

            CategoryCreateBottomSheetContent(
                sheetSelection,
                viewModel,
            ) {
                showBottomSheet = false
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBarWithDeleteAction(
                navController = navController,
                title = stringResource(id = R.string.category),
                actionId = categoryId
            ) {
                showDeleteDialog = true
            }
        }
    ) { innerPadding ->

        val name by viewModel.name.collectAsState()
        val nameErrorMessage by viewModel.nameErrorMessage.collectAsState()
        val colorValue by viewModel.colorValue.collectAsState()
        val iconValue by viewModel.icon.collectAsState()
        val selectedCategoryType by viewModel.categoryType.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            CategoryCreateScreen(
                modifier = Modifier.padding(innerPadding),
                selectedColor = colorValue,
                selectedIcon = iconValue,
                name = name,
                nameErrorMessage = nameErrorMessage,
                selectedCategoryType = selectedCategoryType,
                onCategoryTypeChange = viewModel::setCategoryType,
                onNameChange = {
                    viewModel.setNameChange(it)
                },
                openColorPicker = {
                    scope.launch {
                        if (sheetSelection != 2) {
                            sheetSelection = 2
                        }
                        showBottomSheet = true
                    }
                },
                openIconPicker = {
                    scope.launch {
                        if (sheetSelection != 1) {
                            sheetSelection = 1
                        }
                        showBottomSheet = true
                    }
                }
            )

            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onClick = viewModel::saveOrUpdateCategory
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_done),
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
private fun CategoryCreateBottomSheetContent(
    sheetSelection: Int,
    viewModel: CategoryCreateViewModel,
    hideBottomSheet: () -> Unit
) {
    val context = LocalContext.current

    if (sheetSelection == 1) {
        IconSelectionScreen {
            viewModel.setIcon(context.resources.getResourceName(it))
            hideBottomSheet.invoke()
        }
    } else {
        ColorSelectionScreen {
            viewModel.setColorValue(it)
            hideBottomSheet.invoke()
        }
    }
}


@Composable
private fun CategoryCreateScreen(
    onCategoryTypeChange: ((CategoryType) -> Unit),
    modifier: Modifier = Modifier,
    selectedCategoryType: CategoryType = CategoryType.EXPENSE,
    name: String = "",
    nameErrorMessage: UiText? = null,
    selectedColor: String = "#000000",
    selectedIcon: String = "ic_calendar",
    openIconPicker: (() -> Unit)? = null,
    openColorPicker: (() -> Unit)? = null,
    onNameChange: ((String) -> Unit)? = null
) {

    val context = LocalContext.current

    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {

        CategoryTypeSelectionView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            selectedCategoryType = selectedCategoryType,
            onCategoryTypeChange = onCategoryTypeChange
        )

        OutlinedTextField(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                .fillMaxWidth(),
            value = name,
            singleLine = true,
            label = {
                Text(text = stringResource(id = R.string.category_name))
            },
            onValueChange = {
                onNameChange?.invoke(it)
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus(force = true)
                }
            ),
            isError = nameErrorMessage != null,
            supportingText = if (nameErrorMessage != null) {
                { Text(text = nameErrorMessage.asString(context)) }
            } else {
                null
            }
        )

        IconAndColorComponent(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            selectedColor = selectedColor,
            selectedIcon = selectedIcon,
            openColorPicker = openColorPicker,
            openIconPicker = openIconPicker
        )
    }
}

@Preview
@Composable
private fun CategoryCreateStatePreview() {
    ExpenseManagerTheme {
        CategoryCreateScreen(
            onCategoryTypeChange = {

            }
        )
    }
}