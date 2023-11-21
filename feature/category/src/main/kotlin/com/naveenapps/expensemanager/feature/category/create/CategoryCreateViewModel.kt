package com.naveenapps.expensemanager.feature.category.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
import com.naveenapps.expensemanager.core.domain.usecase.category.AddCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.DeleteCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.FindCategoryByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.UpdateCategoryUseCase
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.feature.category.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _message = MutableSharedFlow<UiText>()
    val message = _message.asSharedFlow()

    private val _showDelete = MutableStateFlow(false)
    val showDelete = _showDelete.asStateFlow()

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
        readCategoryInfo(
            savedStateHandle.get<String>(
                ExpenseManagerScreens.CategoryCreate.KEY_CATEGORY_ID
            )
        )
    }

    private fun updateCategoryInfo(category: Category?) {

        this.category = category

        this.category?.let { categoryItem ->
            name.value = categoryItem.name
            categoryType.value = categoryItem.type
            colorValue.value = categoryItem.storedIcon.backgroundColor
            icon.value = categoryItem.storedIcon.name
            _showDelete.value = true
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
                        _message.emit(
                            UiText.StringResource(R.string.category_delete_error_message)
                        )
                    }

                    is Resource.Success -> {
                        closePage()
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
            storedIcon = StoredIcon(
                name = icon.value,
                backgroundColor = color,
            ),
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
                    _message.emit(UiText.StringResource(R.string.category_create_error))
                }

                is Resource.Success -> {
                    closePage()
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

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "ic_calendar"
    }
}