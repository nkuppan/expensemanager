package com.naveenapps.expensemanager.feature.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.locale.GetCurrentLocaleUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.locale.GetLocalesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.locale.SaveLocaleUseCase
import com.naveenapps.expensemanager.core.model.AppLocale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class LanguageViewModel(
    getCurrentLocaleUseCase: GetCurrentLocaleUseCase,
    getLocalesUseCase: GetLocalesUseCase,
    private val saveLocaleUseCase: SaveLocaleUseCase,
) : ViewModel() {

    private val _currentLocale = MutableStateFlow(
        AppLocale("", R.string.choose_language),
    )
    val currentLocale = _currentLocale.asStateFlow()

    private val _locales = MutableStateFlow<List<AppLocale>>(emptyList())
    val locales = _locales.asStateFlow()

    init {
        getCurrentLocaleUseCase.invoke().onEach {
            _currentLocale.value = it
        }.launchIn(viewModelScope)

        _locales.value = getLocalesUseCase.invoke()
    }

    fun setLocale(locale: AppLocale?) {
        locale ?: return
        viewModelScope.launch {
            saveLocaleUseCase.invoke(locale)
        }
    }
}
