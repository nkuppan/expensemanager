package com.naveenapps.expensemanager.feature.category.create

import android.net.Uri
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
import com.naveenapps.expensemanager.core.repository.ImageStorageRepository
import java.util.Calendar
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryCreateViewModel(
    savedStateHandle: SavedStateHandle,
    private val findCategoryByIdUseCase: FindCategoryByIdUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val imageStorageRepository: ImageStorageRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CategoryCreateState(
            name = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setNameChange,
            ),
            type = TextFieldValue(
                value = CategoryType.EXPENSE,
                valueError = false,
                onValueChange = this::setCategoryTypeChange,
            ),
            color = TextFieldValue(
                value = DEFAULT_COLOR,
                valueError = false,
                onValueChange = this::setColorChange,
            ),
            icon = TextFieldValue(
                value = DEFAULT_ICON,
                valueError = false,
                onValueChange = this::setIconChange,
            ),
            showDeleteDialog = false,
            showDeleteButton = false,
        ),
    )
    val state = _state.asStateFlow()

    private var category: Category? = null

    // Every file created by an image pick/capture during this editing session, so an unsaved
    // session (user backs out, or picks a second photo before saving the first) doesn't leave
    // orphaned files behind. The path that actually ends up persisted is removed from this list
    // at save time; everything left over gets deleted.
    private val sessionCreatedImagePaths = mutableListOf<String>()

    init {
        readCategoryInfo(
            savedStateHandle.get<String>(ExpenseManagerArgsNames.ID),
        )
    }

    /** Destination Uri for the camera app to write a full-resolution capture into. */
    fun createImageCaptureUri(): Uri = imageStorageRepository.createImageCaptureUri()

    private fun updateCategoryInfo(category: Category?) {
        category?.let { categoryItem ->
            this.category = categoryItem
            _state.update {
                it.copy(
                    name = it.name.copy(value = categoryItem.name),
                    type = it.type.copy(value = categoryItem.type),
                    icon = it.icon.copy(value = categoryItem.storedIcon.name),
                    color = it.color.copy(value = categoryItem.storedIcon.backgroundColor),
                    showDeleteButton = true,
                    nameResId = categoryItem.titleResId,
                    customImagePath = categoryItem.storedIcon.customImagePath,
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

    private fun deleteCategory() {
        viewModelScope.launch {
            category?.let { category ->
                when (deleteCategoryUseCase.invoke(category)) {
                    is Resource.Error -> Unit

                    is Resource.Success -> {
                        category.storedIcon.customImagePath?.let {
                            imageStorageRepository.deleteCategoryImage(it)
                        }
                        discardSessionImages()
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
        val customImagePath: String? = _state.value.customImagePath

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
                customImagePath = customImagePath,
            ),
            createdOn = Calendar.getInstance().time,
            updatedOn = Calendar.getInstance().time,
            // Preserve the default-category marker across edits. Without this, saving any
            // change (even just icon/color) to a built-in category would silently strip its
            // titleResId/defaultCategoryKey and permanently freeze its name in English.
            titleResId = this.category?.titleResId,
        )

        viewModelScope.launch {
            val previouslyPersistedImagePath = this@CategoryCreateViewModel.category?.storedIcon?.customImagePath
            val response = if (this@CategoryCreateViewModel.category != null) {
                updateCategoryUseCase(category)
            } else {
                addCategoryUseCase(category)
            }
            when (response) {
                is Resource.Error -> Unit

                is Resource.Success -> {
                    // The old persisted image is only safe to delete now that the new value has
                    // actually been written — deleting it earlier (e.g. the moment a replacement
                    // photo was picked) would leave a dangling reference if the user backed out
                    // without saving.
                    if (previouslyPersistedImagePath != null && previouslyPersistedImagePath != customImagePath) {
                        imageStorageRepository.deleteCategoryImage(previouslyPersistedImagePath)
                    }
                    if (customImagePath != null) {
                        sessionCreatedImagePaths.remove(customImagePath)
                    }
                    discardSessionImages()
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
        // Picking a stock icon and having a custom photo are mutually exclusive.
        _state.update { it.copy(icon = it.icon.copy(value = icon), customImagePath = null) }
    }

    private fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            val path = imageStorageRepository.saveCategoryImage(uri) ?: return@launch
            sessionCreatedImagePaths.add(path)
            _state.update { it.copy(customImagePath = path) }
        }
    }

    private fun removeImage() {
        _state.update { it.copy(customImagePath = null) }
    }

    private fun setNameChange(name: String) {
        _state.update {
            it.copy(
                name = it.name.copy(value = name, valueError = name.isBlank()),
            )
        }
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    /** Handles the user backing out without saving — discards any photo picked this session. */
    private fun cancelEditing() {
        discardSessionImages()
        closePage()
    }

    private fun discardSessionImages() {
        sessionCreatedImagePaths.forEach { imageStorageRepository.deleteCategoryImage(it) }
        sessionCreatedImagePaths.clear()
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
            is CategoryCreateAction.ImagePicked -> onImagePicked(action.uri)
            CategoryCreateAction.RemoveImage -> removeImage()
            CategoryCreateAction.ShowDeleteDialog -> showDeleteDialog()
            CategoryCreateAction.DismissDeleteDialog -> dismissDeleteDialog()
            CategoryCreateAction.ClosePage -> cancelEditing()
            CategoryCreateAction.Delete -> deleteCategory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Safety net for process death / backgrounding paths that skip ClosePage entirely.
        discardSessionImages()
    }

    companion object {
        private const val DEFAULT_COLOR = "#43A546"
        private const val DEFAULT_ICON = "ic_calendar"
    }
}
