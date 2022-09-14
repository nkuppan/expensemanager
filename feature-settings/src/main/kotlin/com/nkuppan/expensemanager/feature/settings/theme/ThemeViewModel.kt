package com.nkuppan.expensemanager.feature.settings.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.model.Theme
import com.nkuppan.expensemanager.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    fun getThemes() = themeRepository.getThemes()

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            themeRepository.saveTheme(theme)
        }
    }
}
