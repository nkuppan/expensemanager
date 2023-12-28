package com.naveenapps.expensemanager.feature.datefilter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    var dateRangeType by mutableStateOf(
        TextFieldValue(
            value = DateRangeType.THIS_MONTH,
            valueError = false,
            onValueChange = this::setFilterType
        )
    )
        private set

    var showCustomRangeSelection by mutableStateOf(false)
        private set

    var fromDate by mutableStateOf(
        TextFieldValue(
            value = Date(),
            valueError = false,
            onValueChange = this::setFromDate
        )
    )
        private set

    var toDate by mutableStateOf(
        TextFieldValue(
            value = Date(),
            valueError = false,
            onValueChange = this::setToDate
        )
    )
        private set

    var dateRangeFilterTypes by mutableStateOf<List<DateRangeModel>>(emptyList())
        private set

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
                    dateRangeFilterTypes = response.data
                }
            }
        }
    }

    private fun updateFilterType(dateRangeType: DateRangeType) {
        this.dateRangeType = this.dateRangeType.copy(value = dateRangeType)
        showCustomRangeSelection = dateRangeType == DateRangeType.CUSTOM
    }

    fun setFilterType(dateRangeType: DateRangeType?) {
        dateRangeType ?: return
        viewModelScope.launch {
            updateFilterType(dateRangeType)
        }
    }

    fun save() {
        viewModelScope.launch {
            val selectedFilter = dateRangeType.value

            val customRanges =
                if (selectedFilter == DateRangeType.CUSTOM) {
                    listOf(fromDate.value, toDate.value)
                } else {
                    dateRangeFilterTypes.filter {
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
                    customRanges,
                )
            }
        }
    }

    fun setFromDate(date: Date) {
        this.fromDate = this.fromDate.copy(value = date)
    }

    fun setToDate(date: Date) {
        this.toDate = this.toDate.copy(value = date)
    }
}
