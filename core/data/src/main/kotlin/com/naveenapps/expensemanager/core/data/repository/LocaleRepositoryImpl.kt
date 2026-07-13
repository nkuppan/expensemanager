package com.naveenapps.expensemanager.core.data.repository

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.R
import com.naveenapps.expensemanager.core.datastore.LocaleDataStore
import com.naveenapps.expensemanager.core.model.AppLocale
import com.naveenapps.expensemanager.core.repository.LocaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

const val SYSTEM_DEFAULT_LOCALE_TAG = ""

val defaultLocale = AppLocale(
    SYSTEM_DEFAULT_LOCALE_TAG,
    R.string.system_default,
)

class LocaleRepositoryImpl(
    private val dataStore: LocaleDataStore,
    private val dispatchers: AppCoroutineDispatchers,
) : LocaleRepository {

    override suspend fun saveLocale(locale: AppLocale): Boolean = withContext(dispatchers.main) {
        applyLocaleTag(locale.tag)
        withContext(dispatchers.io) {
            dataStore.setLocaleTag(locale.tag)
        }
        true
    }

    override suspend fun applyLocale() = withContext(dispatchers.io) {
        // Re-read whatever tag was last persisted and re-apply it on process start,
        // mirroring ApplyThemeUseCase's role for night mode.
        val persistedTag = dataStore.getLocaleTag(defaultLocale.tag).first()
        withContext(dispatchers.main) {
            applyLocaleTag(persistedTag)
        }
    }

    private fun applyLocaleTag(tag: String) {
        val localeList = if (tag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(tag)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    override fun getSelectedLocale(): Flow<AppLocale> {
        val locales = getLocales()
        return dataStore.getLocaleTag(defaultLocale.tag).map { tag ->
            locales.find { locale -> locale.tag == tag } ?: defaultLocale
        }
    }

    override fun getLocales(): List<AppLocale> {
        return listOf(
            defaultLocale,
            AppLocale("en", R.string.language_english),
            AppLocale("es", R.string.language_spanish),
            AppLocale("fr", R.string.language_french),
            AppLocale("de", R.string.language_german),
            AppLocale("hi", R.string.language_hindi),
        )
    }
}
