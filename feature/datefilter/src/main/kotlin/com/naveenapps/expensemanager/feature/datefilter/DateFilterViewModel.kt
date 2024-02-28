package com.naveenapps.expensemanager.feature.datefilter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetAllDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.SaveDateRangeUseCase
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TextFieldValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DateFilterViewModel @Inject constructor(
    getDateRangeUseCase: GetDateRangeUseCase,
    private val getAllDateRangeUseCase: GetAllDateRangeUseCase,
    private val saveDateRangeUseCase: SaveDateRangeUseCase,
) : ViewModel() {

    private val _dateRangeType = MutableStateFlow(
        TextFieldValue(
            value = DateRangeType.THIS_MONTH,
            valueError = false,
            onValueChange = this::setFilterType
        )
    )
    val dateRangeType = _dateRangeType.asStateFlow()

    private val _showCustomRangeSelection = MutableStateFlow(false)
    val showCustomRangeSelection = _showCustomRangeSelection.asStateFlow()

    private val _fromDate = MutableStateFlow(
        TextFieldValue(
            value = Date(),
            valueError = false,
            onValueChange = this::setFromDate
        )
    )
    val fromDate = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow(
        TextFieldValue(
            value = Date(),
            valueError = false,
            onValueChange = this::setToDate
        )
    )
    val toDate = _toDate.asStateFlow()

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
        _dateRangeType.update { it.copy(value = dateRangeType) }
        _showCustomRangeSelection.update { dateRangeType == DateRangeType.CUSTOM }
    }

    private fun setFilterType(dateRangeType: DateRangeType?) {
        dateRangeType ?: return
        viewModelScope.launch {
            updateFilterType(dateRangeType)
        }
    }

    fun save() {
        viewModelScope.launch {
            val selectedFilter = dateRangeType.value.value

            val customRanges =
                if (selectedFilter == DateRangeType.CUSTOM) {
                    listOf(_fromDate.value.value, _toDate.value.value)
                } else {
                    _dateRangeFilterTypes.value.filter {
                        it.type == selectedFilter
                    }.let { it ->
                        it.firstOrNull()?.dateRanges?.map {
                            Date(it)
                        }
                    }
                }

            if (customRanges != null) {
                saveDateRangeUseCase.invoke(
                    selectedFilter,
                    customRanges,
                )
            }
        }
    }

    fun setFromDate(date: Date) {
        _fromDate.update { it.copy(value = date) }
    }

    fun setToDate(date: Date) {
        _toDate.update { it.copy(value = date) }
    }
}
