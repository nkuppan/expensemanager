package com.nkuppan.expensemanager.presentation.settings.datefilter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.domain.model.FilterType
import com.nkuppan.expensemanager.domain.usecase.settings.filter.GetFilterTypeUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.filter.SaveFilterTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DateFilterViewModel @Inject constructor(
    getFilterTypeUseCase: GetFilterTypeUseCase,
    private val saveFilterTypeUseCase: SaveFilterTypeUseCase,
) : ViewModel() {

    private val _filterType = MutableStateFlow(FilterType.THIS_MONTH)
    val filterType = _filterType.asStateFlow()

    val filterTypes = MutableStateFlow(FilterType.values().map {
        FilterTypeUiModel(
            filterType = it,
            name = it.toString().replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
        )
    }.toList())

    init {
        getFilterTypeUseCase.invoke().onEach {
            _filterType.value = it
        }.launchIn(viewModelScope)
    }

    fun setFilterType(filterType: FilterType?) {
        filterType ?: return
        viewModelScope.launch {
            _filterType.value = filterType
        }
    }

    fun save() {
        viewModelScope.launch {
            saveFilterTypeUseCase.invoke(_filterType.value)
        }
    }
}

data class FilterTypeUiModel(
    val filterType: FilterType,
    val name: String
)
