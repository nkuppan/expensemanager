package com.naveenapps.expensemanager.feature.datefilter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.daterange.GetAllDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.daterange.SaveDateRangeUseCase
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DateFilterViewModel @Inject constructor(
    getDateRangeUseCase: GetDateRangeUseCase,
    private val getAllDateRangeUseCase: GetAllDateRangeUseCase,
    private val saveDateRangeUseCase: SaveDateRangeUseCase,
) : ViewModel() {

    private val _dateRangeType = MutableStateFlow(DateRangeType.THIS_MONTH)
    val dateRangeFilterType = _dateRangeType.asStateFlow()

    private val _fromDate = MutableStateFlow(Date())
    val fromDate = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow(Date())
    val toDate = _toDate.asStateFlow()

    private val _showCustomRangeSelection = MutableStateFlow(false)
    val showCustomRangeSelection = _showCustomRangeSelection.asStateFlow()

    private val _dateRangeFilterTypes = MutableStateFlow<List<DateRangeModel>>(emptyList())
    val dateRangeFilterTypes = _dateRangeFilterTypes.asStateFlow()

    init {
        getDateRangeUseCase.invoke().onEach {
            updateFilterType(it.type)
            updateDateRanges()
        }.launchIn(viewModelScope)

        updateDateRanges()
    }

    private fun updateDateRanges() {
        viewModelScope.launch {
            when (val response = getAllDateRangeUseCase.invoke()) {
                is Resource.Error -> Unit
                is Resource.Success -> {
                    _dateRangeFilterTypes.value = response.data
                }
            }
        }
    }

    private fun updateFilterType(dateRangeType: DateRangeType) {
        _dateRangeType.value = dateRangeType
        _showCustomRangeSelection.value = dateRangeType == DateRangeType.CUSTOM
    }

    fun setFilterType(dateRangeType: DateRangeType?) {
        dateRangeType ?: return
        viewModelScope.launch {
            updateFilterType(dateRangeType)
        }
    }

    fun save() {
        viewModelScope.launch {
            val selectedFilter = _dateRangeType.value

            val customRanges =
                if (selectedFilter == DateRangeType.CUSTOM) {
                    listOf(_fromDate.value, _toDate.value)
                } else {
                    _dateRangeFilterTypes.value.filter {
                        it.type == selectedFilter
                    }.let {
                        it.firstOrNull()?.dateRanges?.map {
                            Date(it)
                        }
                    }
                }

            if (customRanges != null) {
                saveDateRangeUseCase.invoke(
                    selectedFilter,
                    customRanges
                )
            }
        }
    }

    fun setFromDate(date: Date) {
        this._fromDate.value = date
    }

    fun setToDate(date: Date) {
        this._toDate.value = date
    }
}
