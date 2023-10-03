package com.nkuppan.expensemanager.data.repository

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.data.datastore.ThemeDataStore
import com.nkuppan.expensemanager.domain.model.Theme
import com.nkuppan.expensemanager.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ThemeRepositoryImpl @Inject constructor(
    private val dataStore: ThemeDataStore,
    private val dispatchers: AppCoroutineDispatchers
) : ThemeRepository {

    private fun getDefaultTheme(): Theme {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> Theme(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                R.string.system_default
            )

            else -> Theme(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, R.string.set_by_battery_saver)
        }
    }

    override suspend fun saveTheme(theme: Theme): Boolean = withContext(dispatchers.main) {
        val mode = theme.mode
        AppCompatDelegate.setDefaultNightMode(mode)
        withContext(dispatchers.io) {
            dataStore.setTheme(mode)
        }
        true
    }

    override suspend fun applyTheme() = withContext(dispatchers.io) {
        val theme = Theme(AppCompatDelegate.MODE_NIGHT_YES, R.string.dark)
        withContext(dispatchers.main) {
            val mode = theme.mode
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    override fun getSelectedTheme(): Flow<Theme> {
        val defaultTheme = getDefaultTheme()
        val defaultMode = defaultTheme.mode
        val themes = getThemes()
        return dataStore.getTheme(defaultMode).map { mode ->
            themes.find { theme -> theme.mode == mode } ?: defaultTheme
        }
    }

    override fun getThemes(): List<Theme> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> listOf(
                Theme(AppCompatDelegate.MODE_NIGHT_NO, R.string.light),
                Theme(AppCompatDelegate.MODE_NIGHT_YES, R.string.dark),
                Theme(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, R.string.set_by_battery_saver),
                Theme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.system_default)
            )

            else -> listOf(
                Theme(AppCompatDelegate.MODE_NIGHT_NO, R.string.light),
                Theme(AppCompatDelegate.MODE_NIGHT_YES, R.string.dark),
                Theme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.system_default)
            )
        }
    }
}
