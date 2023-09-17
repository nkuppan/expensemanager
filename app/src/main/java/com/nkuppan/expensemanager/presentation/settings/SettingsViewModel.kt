package com.nkuppan.expensemanager.presentation.settings

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatDelegate
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.Theme
import com.nkuppan.expensemanager.domain.usecase.settings.GetReminderStatusUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.UpdateReminderStatusUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.theme.GetThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getSelectedTheme: GetThemeUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getReminderStatusUseCase: GetReminderStatusUseCase,
    private val updateReminderStatusUseCase: UpdateReminderStatusUseCase,
) : ViewModel() {

    private var excelExportJob: Job? = null

    private val _theme: MutableStateFlow<Theme> = MutableStateFlow(
        Theme(
            AppCompatDelegate.MODE_NIGHT_NO,
            R.string.light
        )
    )
    val theme = _theme.asStateFlow()

    private val _currency: MutableStateFlow<Currency> = MutableStateFlow(
        Currency(
            R.string.default_currency_type,
            R.string.default_currency_name,
            R.drawable.currency_dollar
        )
    )
    val currency = _currency.asStateFlow()

    private val _reminderStatus: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val reminderStatus = _reminderStatus.asStateFlow()

    init {
        viewModelScope.launch {
            getSelectedTheme.invoke().collectLatest {
                _theme.value = it
            }
        }

        viewModelScope.launch {
            getCurrencyUseCase.invoke().collectLatest {
                _currency.value = it
            }
        }

        viewModelScope.launch {
            getReminderStatusUseCase.invoke().collectLatest {
                _reminderStatus.value = it
            }
        }
    }

    @SuppressLint("unused")
    fun exportExcelFile() {

        excelExportJob?.cancel()

        excelExportJob = viewModelScope.launch {

        }
    }

    @SuppressLint("unused")
    fun exportAsDatabase(excelFile: DocumentFile) {

        excelExportJob?.cancel()

        excelExportJob = viewModelScope.launch {

        }
    }

    fun importDatabase() {

        excelExportJob?.cancel()

        excelExportJob = viewModelScope.launch {

        }
    }

    fun updateReminderStatus(reminderStatus: Boolean) {
        viewModelScope.launch {
            updateReminderStatusUseCase.invoke(reminderStatus)
        }
    }
}