package com.naveenapps.expensemanager.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.daterange.GetDateRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    getDateRangeUseCase: GetDateRangeUseCase
) : ViewModel() {

    private val _date = MutableStateFlow("")
    val date = _date.asStateFlow()

    init {
        getDateRangeUseCase.invoke().onEach {
            _date.value = "${it.name} (${it.description})"
        }.launchIn(viewModelScope)
    }
}