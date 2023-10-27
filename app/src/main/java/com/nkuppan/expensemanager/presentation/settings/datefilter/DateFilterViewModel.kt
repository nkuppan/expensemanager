package com.nkuppan.expensemanager.presentation.settings.datefilter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.domain.model.DateRangeFilterType
import com.nkuppan.expensemanager.domain.usecase.settings.daterange.GetFilterTypeUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.daterange.SaveFilterTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DateFilterViewModel @Inject constructor(
    getFilterTypeUseCase: GetFilterTypeUseCase,
    private val saveFilterTypeUseCase: SaveFilterTypeUseCase,
) : ViewModel() {

    private val _DateRange_filterType = MutableStateFlow(DateRangeFilterType.THIS_MONTH)
    val filterType = _DateRange_filterType.asStateFlow()

    private val _fromDate = MutableStateFlow(Date())
    val fromDate = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow(Date())
    val toDate = _toDate.asStateFlow()

    private val _showCustomRangeSelection = MutableStateFlow(false)
    val showCustomRangeSelection = _showCustomRangeSelection.asStateFlow()

    val dateRangeFilterTypes = MutableStateFlow(DateRangeFilterType.values().map { it ->
        FilterTypeUiModel(
            dateRangeFilterType = it,
            name = it.toString().replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
        )
    }.toList())

    init {
        getFilterTypeUseCase.invoke().onEach {
            updateFilterType(it)
        }.launchIn(viewModelScope)
    }

    private fun updateFilterType(dateRangeFilterType: DateRangeFilterType) {
        _DateRange_filterType.value = dateRangeFilterType
        _showCustomRangeSelection.value = dateRangeFilterType == DateRangeFilterType.CUSTOM
    }

    fun setFilterType(dateRangeFilterType: DateRangeFilterType?) {
        dateRangeFilterType ?: return
        viewModelScope.launch {
            updateFilterType(dateRangeFilterType)
        }
    }

    fun save() {
        viewModelScope.launch {
            val selectedFilter = _DateRange_filterType.value
            saveFilterTypeUseCase.invoke(
                selectedFilter,
                listOf(
                    _fromDate.value,
                    _toDate.value,
                )
            )
        }
    }

    fun setFromDate(date: Date) {
        this._fromDate.value = date
    }

    fun setToDate(date: Date) {
        this._toDate.value = date
    }
}

data class FilterTypeUiModel(
    val dateRangeFilterType: DateRangeFilterType,
    val name: String
)
