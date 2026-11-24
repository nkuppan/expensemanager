package com.naveenapps.expensemanager.core.model

/**
 * The bottom-bar tab the app opens on (Settings → Defaults → Open the app on). Stored by [name],
 * so never rename an entry. [HOME] is the default.
 */
enum class LandingPage {
    HOME,
    ANALYSIS,
    TRANSACTIONS,
    CATEGORIES,
    ;

    companion object {
        fun fromStored(name: String?): LandingPage = entries.find { it.name == name } ?: HOME
    }
}
