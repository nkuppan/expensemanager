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
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
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

    private val _state = MutableStateFlow(
        CategoryCreateState(
            name = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setNameChange
            ),
            type = TextFieldValue(
                value = CategoryType.EXPENSE,
                valueError = false,
                onValueChange = this::setCategoryTypeChange
            ),
            color = TextFieldValue(
                value = DEFAULT_COLOR,
                valueError = false,
                onValueChange = this::setColorChange
            ),
            icon = TextFieldValue(
                value = DEFAULT_ICON,
                valueError = false,
                onValueChange = this::setIconChange
            ),
            showDeleteDialog = false,
            showDeleteButton = false
        )
    )
    val state = _state.asStateFlow()

    private var category: Category? = null

    init {
        readCategoryInfo(
            savedStateHandle.get<String>(ExpenseManagerArgsNames.ID),
        )
    }

    private fun updateCategoryInfo(category: Category?) {
        category?.let { categoryItem ->
            this.category = categoryItem
            _state.update {
                it.copy(
                    name = it.name.copy(value = categoryItem.name),
                    type = it.type.copy(value = categoryItem.type),
                    icon = it.icon.copy(value = categoryItem.storedIcon.name),
                    color = it.color.copy(value = categoryItem.storedIcon.backgroundColor),
                    showDeleteButton = true
                )
            }
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

    private fun saveOrUpdateCategory() {
        val name: String = _state.value.name.value
        val color: String = _state.value.color.value
        val icon: String = _state.value.icon.value
        val type: CategoryType = _state.value.type.value

        if (name.isBlank()) {
            _state.update { it.copy(name = it.name.copy(valueError = true)) }
            return
        }

        val category = Category(
            id = category?.id ?: UUID.randomUUID().toString(),
            name = name,
            type = type,
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
        _state.update { it.copy(color = it.color.copy(value = colorValue)) }
    }

    private fun setCategoryTypeChange(categoryType: CategoryType) {
        _state.update { it.copy(type = it.type.copy(value = categoryType)) }
    }

    private fun setIconChange(icon: String) {
        _state.update { it.copy(icon = it.icon.copy(value = icon)) }
    }

    private fun setNameChange(name: String) {
        _state.update {
            it.copy(
                name = it.name.copy(value = name, valueError = name.isBlank())
            )
        }
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun showDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    private fun dismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    fun processAction(action: CategoryCreateAction) {
        when (action) {
            CategoryCreateAction.Save -> saveOrUpdateCategory()
            is CategoryCreateAction.SelectColor -> setColorChange(action.color)
            is CategoryCreateAction.SelectIcon -> setIconChange(action.icon)
            CategoryCreateAction.ShowDeleteDialog -> showDeleteDialog()
            CategoryCreateAction.DismissDeleteDialog -> dismissDeleteDialog()
            CategoryCreateAction.ClosePage -> closePage()
            CategoryCreateAction.Delete -> deleteCategory()
        }
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "ic_calendar"
    }
}
