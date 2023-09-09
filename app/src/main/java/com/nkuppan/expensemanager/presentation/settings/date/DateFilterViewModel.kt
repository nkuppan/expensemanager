package com.nkuppan.expensemanager.presentation.settings.date

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.data.usecase.settings.filter.GetFilterRangeUseCase
import com.nkuppan.expensemanager.data.usecase.settings.filter.GetFilterTypeUseCase
import com.nkuppan.expensemanager.data.usecase.settings.filter.SaveFilterTypeUseCase
import com.nkuppan.expensemanager.data.usecase.settings.filter.SetCustomFilterRangeUseCase
import com.nkuppan.expensemanager.data.utils.getDateValue
import com.nkuppan.expensemanager.domain.model.FilterType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DateFilterViewModel @Inject constructor(
    getFilterTypeUseCase: GetFilterTypeUseCase,
    private val saveFilterTypeUseCase: SaveFilterTypeUseCase,
    private val getFilterValueUseCase: GetFilterRangeUseCase,
    private val setCustomFilterRangeUseCase: SetCustomFilterRangeUseCase,
) : ViewModel() {

    private val _filterType = MutableStateFlow(FilterType.THIS_MONTH)
    val filterType = _filterType.asStateFlow()

    private val _customFilterValues = MutableStateFlow(listOf<Long>())
    val customFilterValues = _customFilterValues.asStateFlow()

    private val _lastSelected = Channel<FilterType>()
    val lastSelected = _lastSelected.receiveAsFlow()

    private val _dateSelection = Channel<Pair<CustomType, Date?>>()
    val dateSelection = _dateSelection.receiveAsFlow()

    private var filterTypeReceived = FilterType.THIS_MONTH

    private var startDate: Date? = null
    private var endDate: Date? = null

    init {
        viewModelScope.launch {
            getFilterTypeUseCase.invoke().collectLatest {
                filterTypeReceived = it
                _filterType.value = it

                if (it == FilterType.CUSTOM) {
                    val ranges = getFilterValueUseCase.invoke(FilterType.CUSTOM)
                    _customFilterValues.value = ranges
                    startDate = ranges[0].getDateValue()
                    endDate = ranges[1].getDateValue()
                }
            }
        }
    }

    fun saveFilterType(filterType: FilterType) {
        viewModelScope.launch {
            saveFilterTypeUseCase.invoke(filterType)
        }
    }

    fun showDatePicker(isStartDate: Boolean) {
        viewModelScope.launch {
            val dateValues = getFilterValueUseCase.invoke(FilterType.CUSTOM)

            if (dateValues.isNotEmpty() && dateValues.size >= 2) {
                if (isStartDate) {
                    _dateSelection.send(CustomType.START_DATE to dateValues[0].getDateValue())
                } else {
                    _dateSelection.send(CustomType.END_DATE to dateValues[1].getDateValue())
                }
            }
        }
    }

    fun setDate(dateTime: Date, isStartDate: Boolean) {
        if (isStartDate) {
            startDate = dateTime
        } else {
            endDate = dateTime
        }
    }


    fun setLastSelectedFilterType() {
        viewModelScope.launch {
            _lastSelected.send(filterTypeReceived)
        }
    }

    fun saveCustomFilterType() {

        val selectedStartDate = startDate
        val selectedEndDate = endDate

        if (selectedStartDate == null || selectedEndDate == null) {
            return
        }

        viewModelScope.launch {
            saveFilterType(FilterType.CUSTOM)
            setCustomFilterRangeUseCase.invoke(listOf(selectedStartDate, selectedEndDate))
        }
    }

    fun isLastFilterNotCustom(): Boolean {
        return filterTypeReceived != FilterType.CUSTOM
    }
}

enum class CustomType {
    START_DATE, END_DATE
}