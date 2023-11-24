package com.naveenapps.expensemanager.feature.datefilter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.MoveDateRangeBackwardUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.MoveDateRangeForwardUseCase
import com.naveenapps.expensemanager.core.model.DateRangeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    getDateRangeUseCase: GetDateRangeUseCase,
    private val moveDateRangeBackwardUseCase: MoveDateRangeBackwardUseCase,
    private val moveDateRangeForwardUseCase: MoveDateRangeForwardUseCase,
) : ViewModel() {

    private val _date = MutableStateFlow("")
    val date = _date.asStateFlow()

    private val _showForward = MutableStateFlow(true)
    val showForward = _showForward.asStateFlow()

    private val _showBackward = MutableStateFlow(true)
    val showBackward = _showBackward.asStateFlow()

    private var dateRangeType = DateRangeType.THIS_MONTH

    init {
        getDateRangeUseCase.invoke().onEach {
            dateRangeType = it.type

            _date.value = it.description

            when (it.type) {
                DateRangeType.TODAY,
                DateRangeType.THIS_WEEK,
                DateRangeType.THIS_MONTH,
                DateRangeType.THIS_YEAR -> {
                    _showForward.value = true
                    _showBackward.value = true
                }

                DateRangeType.ALL,
                DateRangeType.CUSTOM -> {
                    _showForward.value = false
                    _showBackward.value = false
                }
            }
        }.launchIn(viewModelScope)
    }

    fun moveDateRangeForward() {
        viewModelScope.launch {
            moveDateRangeForwardUseCase.invoke(dateRangeType)
        }
    }

    fun moveDateRangeBackward() {
        viewModelScope.launch {
            moveDateRangeBackwardUseCase.invoke(dateRangeType)
        }
    }
}