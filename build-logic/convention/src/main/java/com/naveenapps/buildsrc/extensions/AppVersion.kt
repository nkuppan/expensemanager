package com.naveenapps.buildsrc.extensions

import java.util.Properties
import org.gradle.api.Project

/** File at the repository root that holds the app's version name. */
const val VERSION_FILE = "version.properties"
const val VERSION_KEY = "versionName"

/**
 * The app's semantic version. [code] is derived from it (1.4.27 → 1_004_027), so a bump always
 * gives Play a higher version code and an upload can't fail with "Version code is too low".
 */
data class AppVersion(val major: Int, val minor: Int, val patch: Int) {

    init {
        require(major >= 0 && minor in 0..MAX_PART && patch in 0..MAX_PART) {
            "Version $major.$minor.$patch: minor and patch must be 0..$MAX_PART to keep version codes unique"
        }
    }

    val name: String get() = "$major.$minor.$patch"

    val code: Int get() = 1_000_000 * major + 1_000 * minor + patch

    fun bump(part: String): AppVersion = when (part.trim().lowercase()) {
        "major" -> AppVersion(major + 1, 0, 0)
        "minor" -> AppVersion(major, minor + 1, 0)
        "patch" -> AppVersion(major, minor, patch + 1)
        else -> throw IllegalArgumentException("Unknown bump \"$part\": use patch, minor or major")
    }

    companion object {
        private const val MAX_PART = 999

        fun parse(name: String): AppVersion {
            val parts = name.trim().split(".").map { it.toIntOrNull() }
            require(parts.size == 3 && parts.all { it != null }) { "Version \"$name\" isn't MAJOR.MINOR.PATCH" }
            return AppVersion(parts[0]!!, parts[1]!!, parts[2]!!)
        }

        /** Reads [VERSION_KEY] from the text of [VERSION_FILE]. */
        fun fromProperties(text: String): AppVersion {
            val properties = Properties().apply { load(text.reader()) }
            val name = properties.getProperty(VERSION_KEY)
                ?: throw IllegalStateException("$VERSION_FILE has no $VERSION_KEY")
            return parse(name)
        }
    }
}

/** The version in [VERSION_FILE], read through Gradle so the configuration cache tracks it. */
fun Project.appVersion(): AppVersion = providers
    .fileContents(rootProject.layout.projectDirectory.file(VERSION_FILE))
    .asText
    .map { AppVersion.fromProperties(it) }
    .get()
