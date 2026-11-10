package com.naveenapps.expensemanager.feature.about.licenses

import java.util.Locale

/** One library bundled in the app and its licence: the full text, or a link to it. */
data class OpenSourceLicense(
    val name: String,
    val text: String,
) {
    /** Many libraries only ship a link to their licence instead of its text. */
    val url: String? get() = text.takeIf { URL.matches(it) }

    val kind: LicenseKind get() = LicenseKind.of(text)

    private companion object {
        val URL = Regex("""^https?://\S+$""")
    }
}

/** The common licences, recognised from their text or link so the list can name them. */
enum class LicenseKind(val label: String?) {
    APACHE_2("Apache License 2.0"),
    MIT("MIT License"),
    BSD("BSD License"),
    EPL("Eclipse Public License"),
    MPL("Mozilla Public License"),
    LGPL("GNU Lesser General Public License"),
    ANDROID_SDK("Android Software Development Kit License"),
    PLAY_CORE("Play Core Software Development Kit Terms"),
    OTHER(null),
    ;

    companion object {
        fun of(text: String): LicenseKind {
            val lower = text.lowercase(Locale.ROOT)
            return when {
                "apache.org/licenses/license-2.0" in lower || ("apache license" in lower && "2.0" in lower) -> APACHE_2

                "opensource.org/licenses/mit" in lower || "mit license" in lower ||
                    "permission is hereby granted, free of charge" in lower -> MIT

                "bsd" in lower || "redistribution and use in source and binary forms" in lower -> BSD

                "eclipse public license" in lower || "eclipse.org/legal/epl" in lower -> EPL

                "mozilla public license" in lower || "mozilla.org/mpl" in lower -> MPL

                "gnu lesser general public license" in lower || "licenses/lgpl" in lower -> LGPL

                "developer.android.com/studio/terms" in lower -> ANDROID_SDK

                "playcore/license" in lower -> PLAY_CORE

                else -> OTHER
            }
        }
    }
}

/**
 * Reads the two files the oss-licenses Gradle plugin generates: `third_party_license_metadata`
 * has one line per library, "offset:length Library name", pointing into the UTF-8 bytes of
 * `third_party_licenses`. Broken lines are skipped; a library listed twice is shown once.
 */
object OpenSourceLicenseParser {

    fun parse(metadata: String, licenses: ByteArray): List<OpenSourceLicense> = metadata.lineSequence()
        .mapNotNull { parseLine(it, licenses) }
        .distinctBy { it.name.lowercase(Locale.ROOT) }
        .sortedBy { it.name.lowercase(Locale.ROOT) }
        .toList()

    private fun parseLine(line: String, licenses: ByteArray): OpenSourceLicense? {
        val space = line.indexOf(' ')
        if (space <= 0) return null
        val position = line.substring(0, space).split(':').mapNotNull { it.toIntOrNull() }
        if (position.size != 2) return null
        val (offset, length) = position
        if (offset < 0 || length <= 0 || offset + length > licenses.size) return null
        val name = line.substring(space + 1).trim()
        val text = String(licenses, offset, length, Charsets.UTF_8).trim()
        if (name.isEmpty() || text.isEmpty()) return null
        return OpenSourceLicense(name = name, text = text)
    }
}
