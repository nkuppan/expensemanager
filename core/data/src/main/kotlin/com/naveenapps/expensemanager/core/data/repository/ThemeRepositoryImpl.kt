package com.naveenapps.expensemanager.core.data.repository

import androidx.appcompat.app.AppCompatDelegate
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.R
import com.naveenapps.expensemanager.core.datastore.ThemeDataStore
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.repository.ThemeRepository
import com.naveenapps.expensemanager.core.repository.VersionCheckerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

val defaultTheme = Theme(
    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
    R.string.system_default,
)

class ThemeRepositoryImpl(
    private val dataStore: ThemeDataStore,
    private val versionCheckerRepository: VersionCheckerRepository,
    private val dispatchers: AppCoroutineDispatchers,
) : ThemeRepository {

    private fun getDefaultTheme(): Theme {
        return defaultTheme
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
            versionCheckerRepository.isAndroidQAndAbove() -> listOf(
                Theme(AppCompatDelegate.MODE_NIGHT_NO, R.string.light),
                Theme(AppCompatDelegate.MODE_NIGHT_YES, R.string.dark),
                Theme(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, R.string.set_by_battery_saver),
                Theme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.system_default),
            )

            else -> listOf(
                Theme(AppCompatDelegate.MODE_NIGHT_NO, R.string.light),
                Theme(AppCompatDelegate.MODE_NIGHT_YES, R.string.dark),
                Theme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, R.string.system_default),
            )
        }
    }
}
