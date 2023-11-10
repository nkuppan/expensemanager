package com.naveenapps.expensemanager.presentation.settings.theme

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.domain.usecase.settings.theme.GetCurrentThemeUseCase
import com.naveenapps.expensemanager.domain.usecase.settings.theme.GetThemesUseCase
import com.naveenapps.expensemanager.domain.usecase.settings.theme.SaveThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ThemeViewModel @Inject constructor(
    getSelectedTheme: GetCurrentThemeUseCase,
    getThemesUseCase: GetThemesUseCase,
    private val saveThemeUseCase: SaveThemeUseCase,
) : ViewModel() {

    private val _currentTheme =
        MutableStateFlow(
            Theme(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                com.naveenapps.expensemanager.core.data.R.string.system_default
            )
        )
    val currentTheme = _currentTheme.asStateFlow()

    private val _themes = MutableStateFlow<List<Theme>>(emptyList())
    val themes = _themes.asStateFlow()

    init {
        getSelectedTheme.invoke().onEach {
            _currentTheme.value = it
        }.launchIn(viewModelScope)

        _themes.value = getThemesUseCase.invoke()
    }

    fun setTheme(theme: Theme?) {
        theme ?: return
        viewModelScope.launch {
            saveThemeUseCase.invoke(theme)
        }
    }
}
