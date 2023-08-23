package com.nkuppan.expensemanager.feature.category.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.CategoryType
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.data.usecase.category.AddCategoryUseCase
import com.nkuppan.expensemanager.data.usecase.category.DeleteCategoryUseCase
import com.nkuppan.expensemanager.data.usecase.category.UpdateCategoryUseCase
import com.nkuppan.expensemanager.feature.category.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@HiltViewModel
class CategoryCreateViewModel @Inject constructor(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private val _categoryCreated = Channel<Unit>()
    val categoryCreated = _categoryCreated.receiveAsFlow()

    private val _colorPicker = Channel<Unit>()
    val colorPicker = _colorPicker.receiveAsFlow()

    private val _categoryType = MutableStateFlow(CategoryType.EXPENSE)
    val categoryType = _categoryType.asStateFlow()

    val categoryName: MutableLiveData<String> = MutableLiveData()

    val categoryNameErrorText: MutableLiveData<String> = MutableLiveData()

    val isFavorite: MutableLiveData<Boolean> = MutableLiveData()

    val colorValue: MutableLiveData<String> = MutableLiveData()

    private var categoryItem: Category? = null

    private var currentType: CategoryType = CategoryType.EXPENSE

    init {
        colorValue.value = "#43A546"

        isFavorite.value = false
    }

    fun setCategoryValue(aCategory: Category?) {

        this.categoryItem = aCategory

        if (categoryItem == null) {
            return
        }

        categoryItem?.let { categoryItem ->

            categoryName.value = categoryItem.name

            _categoryType.value = categoryItem.type

            currentType = categoryItem.type

            colorValue.value = categoryItem.backgroundColor

            isFavorite.value = categoryItem.isFavorite
        }
    }

    fun delete() {
        viewModelScope.launch {
            categoryItem?.let { category ->
                when (deleteCategoryUseCase.invoke(category)) {
                    is Resource.Error -> {
                        _errorMessage.send(UiText.StringResource(R.string.category_delete_error_message))
                    }

                    is Resource.Success -> {
                        _categoryCreated.send(Unit)
                    }
                }
            }
        }
    }

    fun onColorContainerClick() {
        viewModelScope.launch {
            _colorPicker.send(Unit)
        }
    }

    fun onSaveClick() {

        val name: String? = categoryName.value
        val color: String? = colorValue.value

        if (name.isNullOrBlank() || color == null) {
            viewModelScope.launch {
                _errorMessage.send(UiText.StringResource(R.string.category_create_error))
            }
            return
        }

        val category = Category(
            id = categoryItem?.id ?: UUID.randomUUID().toString(),
            name = name,
            type = currentType,
            backgroundColor = color,
            isFavorite = isFavorite.value!!,
            createdOn = Calendar.getInstance().time,
            updatedOn = Calendar.getInstance().time
        )

        viewModelScope.launch {
            val response = if (categoryItem != null) {
                updateCategoryUseCase(category)
            } else {
                addCategoryUseCase(category)
            }
            when (response) {
                is Resource.Error -> {
                    _errorMessage.send(UiText.StringResource(R.string.category_create_error))
                }

                is Resource.Success -> {
                    _categoryCreated.send(Unit)
                }
            }
        }
    }

    fun setColorValue(aColor: Int) {
        colorValue.value = String.format("#%06X", 0xFFFFFF and aColor)
    }

    fun setCategoryType(type: CategoryType) {
        this.currentType = type
    }
}