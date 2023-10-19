package com.nkuppan.expensemanager.presentation.settings.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.domain.model.ExportFileType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.usecase.settings.export.ExportFileUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.filter.GetFilterRangeDateStringUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.filter.GetFilterTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    getFilterTypeUseCase: GetFilterTypeUseCase,
    getFilterRangeDateStringUseCase: GetFilterRangeDateStringUseCase,
    private val exportFileUseCase: ExportFileUseCase,
) : ViewModel() {

    private val _selectedDateRange = MutableStateFlow<String?>(null)
    val selectedDateRange = _selectedDateRange.asStateFlow()

    private val _exportFileType = MutableStateFlow(ExportFileType.CSV)
    val exportFileType = _exportFileType.asStateFlow()

    init {
        getFilterTypeUseCase.invoke().map {
            _selectedDateRange.value = getFilterRangeDateStringUseCase.invoke(it)
        }.launchIn(viewModelScope)
    }

    fun setExportFileType(exportFileType: ExportFileType) {
        this._exportFileType.value = exportFileType
    }

    fun export() {
        viewModelScope.launch {
            when (val response = exportFileUseCase.invoke(_exportFileType.value)) {
                is Resource.Error -> {

                }

                is Resource.Success -> {
                    if (response.data) {

                    }
                }
            }
        }
    }
}