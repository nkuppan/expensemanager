package com.nkuppan.expensemanager.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.domain.usecase.transaction.AnalysisData
import com.nkuppan.expensemanager.domain.usecase.transaction.GetChartDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AnalysisScreenViewModel @Inject constructor(
    getChartDataUseCase: GetChartDataUseCase
) : ViewModel() {

    private val _graphItems = MutableStateFlow<UiState<AnalysisData>>(UiState.Loading)
    val graphItems = _graphItems.asStateFlow()

    init {
        getChartDataUseCase.invoke().onEach { response ->
            _graphItems.value = UiState.Success(response)
        }.launchIn(viewModelScope)
    }
}
