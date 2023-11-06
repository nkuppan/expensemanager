package com.nkuppan.expensemanager.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.domain.usecase.settings.daterange.GetSelectedFilterNameAndDateRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    getSelectedFilterNameAndDateRangeUseCase: GetSelectedFilterNameAndDateRangeUseCase
) : ViewModel() {

    private val _date = MutableStateFlow("")
    val date = _date.asStateFlow()

    init {
        getSelectedFilterNameAndDateRangeUseCase.invoke().onEach {
            _date.value = it
        }.launchIn(viewModelScope)
    }
}