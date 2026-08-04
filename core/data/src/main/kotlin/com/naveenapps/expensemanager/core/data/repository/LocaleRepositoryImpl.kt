package com.naveenapps.expensemanager.core.data.repository

import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
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
        // Ordered by actual on-device/app language usage rather than raw speaker counts. These
        // diverge in populous markets where a large share of smartphone users default to English
        // for apps despite it not being the majority spoken language (India, Nigeria, Philippines,
        // Pakistan), versus markets like Indonesia, Brazil, and Japan where the local language is
        // used on-device almost universally. System default and English stay first since one is a
        // meta-option and the other is the app's base/fallback language.
        return listOf(
            defaultLocale,
            AppLocale("en", R.string.language_english),
            AppLocale("zh", R.string.language_chinese),
            AppLocale("es", R.string.language_spanish),
            AppLocale("id", R.string.language_indonesian),
            AppLocale("pt", R.string.language_portuguese),
            AppLocale("hi", R.string.language_hindi),
            AppLocale("ar", R.string.language_arabic),
            AppLocale("ru", R.string.language_russian),
            AppLocale("fr", R.string.language_french),
            AppLocale("ja", R.string.language_japanese),
            AppLocale("vi", R.string.language_vietnamese),
            AppLocale("tr", R.string.language_turkish),
            AppLocale("de", R.string.language_german),
            AppLocale("ko", R.string.language_korean),
            AppLocale("it", R.string.language_italian),
            AppLocale("pl", R.string.language_polish),
        ).withDeviceLocaleFirst()
    }

    /**
     * Surfaces whichever supported language matches the device's actual system locale (read off
     * [Resources.getSystem], which reflects the OS-level configuration and is unaffected by any
     * per-app locale override this app has already applied) right after the "System default"
     * entry, so a user picking a language for the first time can spot their own without scrolling
     * the full list. No-ops if nothing matches, or if the match is already in that position.
     */
    private fun List<AppLocale>.withDeviceLocaleFirst(): List<AppLocale> {
        val deviceLanguage = ConfigurationCompat.getLocales(Resources.getSystem().configuration)
            .get(0)
            ?.language
            ?: return this
        val matchIndex = indexOfFirst { it.tag == deviceLanguage }
        if (matchIndex <= 1) return this
        return toMutableList().apply {
            add(1, removeAt(matchIndex))
        }
    }
}
