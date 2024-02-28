package com.naveenapps.expensemanager.feature.category.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.category.AddCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.DeleteCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.FindCategoryByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.UpdateCategoryUseCase
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CategoryCreateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val findCategoryByIdUseCase: FindCategoryByIdUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _isDeleteEnabled = MutableStateFlow(false)
    val isDeleteEnabled = _isDeleteEnabled.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog = _showDeleteDialog.asStateFlow()

    var categoryTypeField = MutableStateFlow(
        TextFieldValue(
            value = CategoryType.EXPENSE,
            valueError = false,
            onValueChange = this::setCategoryTypeChange
        )
    )
        private set

    var nameField = MutableStateFlow(
        TextFieldValue(
            value = "",
            valueError = false,
            onValueChange = this::setNameChange
        )
    )
        private set


    var selectedColorField = MutableStateFlow(
        TextFieldValue(
            value = DEFAULT_COLOR,
            valueError = false,
            onValueChange = this::setColorChange
        )
    )
        private set

    var selectedIconField = MutableStateFlow(
        TextFieldValue(
            value = DEFAULT_ICON,
            valueError = false,
            onValueChange = this::setIconChange
        )
    )
        private set

    private var category: Category? = null

    init {
        readCategoryInfo(
            savedStateHandle.get<String>(
                ExpenseManagerScreens.CategoryCreate.KEY_CATEGORY_ID,
            ),
        )
    }

    private fun updateCategoryInfo(category: Category?) {
        category?.let { categoryItem ->
            this.category = categoryItem
            nameField.update { it.copy(value = categoryItem.name) }
            categoryTypeField.update { it.copy(value = categoryItem.type) }
            selectedColorField.update { it.copy(value = categoryItem.storedIcon.backgroundColor) }
            selectedIconField.update { it.copy(value = categoryItem.storedIcon.name) }
            _isDeleteEnabled.value = true
        }
    }

    private fun readCategoryInfo(categoryId: String?) {
        categoryId ?: return
        viewModelScope.launch {
            when (val response = findCategoryByIdUseCase.invoke(categoryId)) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    updateCategoryInfo(response.data)
                }
            }
        }
    }

    fun deleteCategory() {
        viewModelScope.launch {
            category?.let { category ->
                when (deleteCategoryUseCase.invoke(category)) {
                    is Resource.Error -> Unit
                    is Resource.Success -> {
                        closePage()
                    }
                }
            }
        }
    }

    fun saveOrUpdateCategory() {
        val name: String = nameField.value.value
        val color: String = selectedColorField.value.value
        val icon: String = selectedIconField.value.value

        if (name.isBlank()) {
            nameField.update { it.copy(valueError = true) }
            return
        }

        val category = Category(
            id = category?.id ?: UUID.randomUUID().toString(),
            name = name,
            type = categoryTypeField.value.value,
            storedIcon = StoredIcon(
                name = icon,
                backgroundColor = color,
            ),
            createdOn = Calendar.getInstance().time,
            updatedOn = Calendar.getInstance().time,
        )

        viewModelScope.launch {
            val response = if (this@CategoryCreateViewModel.category != null) {
                updateCategoryUseCase(category)
            } else {
                addCategoryUseCase(category)
            }
            when (response) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    closePage()
                }
            }
        }
    }

    private fun setColorChange(colorValue: String) {
        selectedColorField.update { it.copy(value = colorValue) }
    }

    private fun setCategoryTypeChange(categoryType: CategoryType) {
        categoryTypeField.update { it.copy(value = categoryType) }
    }

    private fun setIconChange(icon: String) {
        selectedIconField.update { it.copy(value = icon) }
    }

    private fun setNameChange(name: String) {
        nameField.update { it.copy(value = name) }
        nameField.update { it.copy(valueError = name.isBlank()) }
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun openDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun closeDeleteDialog() {
        _showDeleteDialog.value = false
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "ic_calendar"
    }
}
