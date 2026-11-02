package com.naveenapps.expensemanager.feature.transaction.search

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.repository.LocaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * The name each category is shown with, so search can match what the user actually sees. Built-in
 * categories store an English name and are displayed translated (their `titleResId`).
 */
fun interface CategoryDisplayNames {

    /** A name lookup, re-emitted whenever the app's language changes. */
    fun lookup(): Flow<(Category) -> String>

    companion object {
        /** Stored names only (built-in categories in English). For tests and previews. */
        val Stored = CategoryDisplayNames { flowOf { category: Category -> category.name } }
    }
}

/**
 * Reads built-in category names in the language picked in Settings → Language. It builds a context
 * for that language instead of using the application context's own: before Android 13, AppCompat's
 * in-app language only reaches activity contexts. A blank tag means "system default".
 */
internal class LocalizedCategoryDisplayNames(
    private val context: Context,
    private val localeRepository: LocaleRepository,
) : CategoryDisplayNames {

    override fun lookup(): Flow<(Category) -> String> = localeRepository.getSelectedLocale().map { locale ->
        val localized = if (locale.tag.isBlank()) {
            context
        } else {
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocales(LocaleList.forLanguageTags(locale.tag))
            context.createConfigurationContext(configuration)
        }
        val displayName: (Category) -> String = { category ->
            category.titleResId?.let { localized.getString(it) } ?: category.name
        }
        displayName
    }
}
