package com.nkuppan.expensemanager.presentation.category.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.usecase.category.AddCategoryUseCase
import com.nkuppan.expensemanager.domain.usecase.category.DeleteCategoryUseCase
import com.nkuppan.expensemanager.domain.usecase.category.FindCategoryByIdUseCase
import com.nkuppan.expensemanager.domain.usecase.category.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<UiText>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _categoryUpdated = MutableSharedFlow<Boolean>()
    val categoryUpdated = _categoryUpdated.asSharedFlow()

    var categoryType = MutableStateFlow(CategoryType.EXPENSE)
        private set

    var name = MutableStateFlow("")
        private set

    var nameErrorMessage = MutableStateFlow<UiText?>(null)
        private set

    var colorValue = MutableStateFlow(DEFAULT_COLOR)
        private set

    var icon = MutableStateFlow(DEFAULT_ICON)
        private set

    private var category: Category? = null

    init {
        readCategoryInfo(savedStateHandle.get<String>(CATEGORY_ID))
    }

    private fun updateCategoryInfo(category: Category?) {

        this.category = category

        this.category?.let { categoryItem ->
            name.value = categoryItem.name
            categoryType.value = categoryItem.type
            colorValue.value = categoryItem.iconBackgroundColor
            icon.value = categoryItem.iconName
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
                    is Resource.Error -> {
                        _errorMessage.emit(
                            UiText.StringResource(R.string.category_delete_error_message)
                        )
                    }

                    is Resource.Success -> {
                        _categoryUpdated.emit(true)
                    }
                }
            }
        }
    }

    fun saveOrUpdateCategory() {

        val name: String = name.value
        val color: String = colorValue.value

        if (name.isBlank()) {
            nameErrorMessage.value = UiText.StringResource(R.string.category_name_error)
            return
        }

        val category = Category(
            id = category?.id ?: UUID.randomUUID().toString(),
            name = name,
            type = categoryType.value,
            iconBackgroundColor = color,
            iconName = icon.value,
            createdOn = Calendar.getInstance().time,
            updatedOn = Calendar.getInstance().time
        )

        viewModelScope.launch {
            val response = if (this@CategoryCreateViewModel.category != null) {
                updateCategoryUseCase(category)
            } else {
                addCategoryUseCase(category)
            }
            when (response) {
                is Resource.Error -> {
                    _errorMessage.emit(UiText.StringResource(R.string.category_create_error))
                }

                is Resource.Success -> {
                    _categoryUpdated.emit(true)
                }
            }
        }
    }

    fun setColorValue(colorValue: Int) {
        this.colorValue.value = String.format("#%06X", 0xFFFFFF and colorValue)
    }

    fun setCategoryType(categoryType: CategoryType) {
        this.categoryType.value = categoryType
    }

    fun setIcon(icon: String) {
        this.icon.value = icon
    }

    fun setNameChange(name: String) {
        this.name.value = name
        if (name.isBlank()) {
            nameErrorMessage.value = UiText.StringResource(R.string.category_name_error)
        } else {
            nameErrorMessage.value = null
        }
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "ic_calendar"
        private const val CATEGORY_ID = "categoryId"
    }
}