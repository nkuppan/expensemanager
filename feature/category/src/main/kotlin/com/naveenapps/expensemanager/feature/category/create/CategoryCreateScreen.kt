package com.naveenapps.expensemanager.feature.category.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomActionBar
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmDeleteConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSection
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextField
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.utils.rememberImagePickerActions
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.feature.category.R
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryCreateScreen(
    viewModel: CategoryCreateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { EmSnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.errorMessage) { messageRes ->
        coroutineScope.launch { snackbarHostState.showSnackbar(context.getString(messageRes)) }
    }

    val imagePicker = rememberImagePickerActions(
        createCaptureUri = viewModel::createImageCaptureUri,
        onImagePicked = { viewModel.processAction(CategoryCreateAction.ImagePicked(it)) },
    )

    CategoryCreateScreenContentView(
        state = state,
        onAction = viewModel::processAction,
        onCaptureRequested = imagePicker.onCaptureRequested,
        onGalleryRequested = imagePicker.onGalleryRequested,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun CategoryCreateScreenContentView(
    state: CategoryCreateState,
    onAction: (CategoryCreateAction) -> Unit,
    onCaptureRequested: () -> Unit = {},
    onGalleryRequested: () -> Unit = {},
    snackbarHostState: EmSnackbarHostState = remember { EmSnackbarHostState() },
) {
    if (state.showDeleteDialog) {
        EmDeleteConfirm(
            onConfirm = { onAction.invoke(CategoryCreateAction.Delete) },
            onDismiss = { onAction.invoke(CategoryCreateAction.DismissDeleteDialog) },
            message = stringResource(R.string.category_delete_message),
        )
    }

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
                    onAction.invoke(CategoryCreateAction.ClosePage)
                },
                title = if (state.showDeleteButton) {
                    stringResource(R.string.edit_category)
                } else {
                    stringResource(R.string.create_category)
                },
                actions = {
                    if (state.showDeleteButton) {
                        EmIconButton(
                            icon = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete),
                            onClick = { onAction.invoke(CategoryCreateAction.ShowDeleteDialog) },
                            tint = EmTheme.colors.scheme.error,
                        )
                    }
                },
            )
        },
        bottomBar = {
            EmBottomActionBar(
                primaryText = stringResource(R.string.save),
                onPrimaryClick = { onAction.invoke(CategoryCreateAction.Save) },
                primaryIcon = Icons.Default.Done,
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
            nameResId = state.nameResId,
            customImagePath = state.customImagePath,
            onCaptureRequested = onCaptureRequested,
            onGalleryRequested = onGalleryRequested,
            onRemoveImage = { onAction.invoke(CategoryCreateAction.RemoveImage) },
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
    nameResId: Int? = null,
    customImagePath: String? = null,
    onCaptureRequested: () -> Unit = {},
    onGalleryRequested: () -> Unit = {},
    onRemoveImage: () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(horizontal = EmTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
    ) {
        EmSection(
            title = stringResource(R.string.category_type),
            modifier = Modifier.padding(top = EmTheme.spacing.sm),
        ) {
            EmCard {
                Column(
                    modifier = Modifier.padding(EmTheme.spacing.lg),
                ) {
                    CategoryTypeSelectionView(
                        modifier = Modifier.fillMaxWidth(),
                        selectedCategoryType = categoryTypeField.value,
                        onCategoryTypeChange = categoryTypeField.onValueChange!!,
                    )
                }
            }
        }

        EmSection(title = stringResource(R.string.details)) {
            EmCard {
                Column(
                    modifier = Modifier.padding(EmTheme.spacing.lg),
                ) {
                    val displayName = nameResId?.let { stringResource(it) } ?: nameField.value
                    EmTextField(
                        value = displayName,
                        // Default categories can't be renamed (the field is disabled too). Call the callback;
                        // `if (..) cb else null?.invoke(it)` only returned it, so typing was dropped.
                        onValueChange = { if (nameResId == null) nameField.onValueChange?.invoke(it) },
                        label = stringResource(id = R.string.category_name),
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameField.valueError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        enabled = nameResId == null,
                        supportingText = if (nameField.valueError) stringResource(id = R.string.category_name_error) else null,
                        selectAllOnFocus = true,
                    )
                    if (nameResId != null) {
                        EmText(
                            text = stringResource(R.string.default_category_name_locked_message),
                            modifier = Modifier.padding(top = EmTheme.spacing.xs),
                            role = EmTextRole.Supporting,
                            color = EmTextColor.Supporting,
                        )
                    }
                }
            }
        }

        EmSection(title = stringResource(R.string.appearance)) {
            EmCard {
                Column(
                    modifier = Modifier.padding(EmTheme.spacing.lg),
                ) {
                    IconAndColorComponent(
                        modifier = Modifier.fillMaxWidth(),
                        selectedColor = selectedColorField.value,
                        selectedIcon = selectedIconField.value,
                        onColorSelection = selectedColorField.onValueChange,
                        onIconSelection = selectedIconField.onValueChange,
                        customImagePath = customImagePath,
                        onCaptureRequested = onCaptureRequested,
                        onGalleryRequested = onGalleryRequested,
                        onRemoveImage = onRemoveImage,
                    )
                }
            }
        }

        // Bottom spacer for FAB clearance
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@EmPreviews
@Composable
private fun CategoryCreateStatePreview() {
    val nameField = TextFieldValue(
        value = "",
        valueError = false,
        onValueChange = { },
    )
    val selectedColorField = TextFieldValue(
        value = "#000000",
        valueError = false,
        onValueChange = { },
    )
    val selectedIconField = TextFieldValue(
        value = "account_balance_wallet",
        valueError = false,
        onValueChange = { },
    )
    val categoryType = TextFieldValue(
        value = CategoryType.EXPENSE,
        valueError = false,
        onValueChange = { },
    )

    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CategoryCreateScreenContentView(
            state = CategoryCreateState(
                name = nameField,
                type = categoryType,
                color = selectedColorField,
                icon = selectedIconField,
                showDeleteButton = false,
                showDeleteDialog = false,
            ),
            onAction = {},
        )
    }
}
