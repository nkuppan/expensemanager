package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.core.model.Theme
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {

    /**
     * Storing the selected theme
     */
    suspend fun saveTheme(theme: Theme): Boolean

    /**
     * Storing the selected theme
     */
    suspend fun applyTheme()


    /**
     * Reading the selected theme
     */
    fun getSelectedTheme(): Flow<Theme>


    /**
     * Reading the list of theme available in the data store
     */
    fun getThemes(): List<Theme>
}