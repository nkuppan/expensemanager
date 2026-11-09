package com.naveenapps.buildsrc.release

import com.naveenapps.buildsrc.extensions.AppVersion
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Warns (never fails) when the version about to be released has no notes: no
 * `whats_new_<version>` array, no entry in WhatsNewReleases.kt, or Play release notes that don't
 * contain the same highlights. Reads the version when it runs, so after bumpVersion it checks the
 * new one.
 */
abstract class CheckReleaseNotesTask : DefaultTask() {

    @get:Internal
    abstract val versionFile: RegularFileProperty

    /** Default-language strings holding the `whats_new_<version>` arrays. */
    @get:Internal
    abstract val whatsNewStrings: RegularFileProperty

    /** The list of releases shown in the notification centre. */
    @get:Internal
    abstract val whatsNewReleases: RegularFileProperty

    /** Play Store release notes in the default language. */
    @get:Internal
    abstract val playReleaseNotes: RegularFileProperty

    @TaskAction
    fun check() {
        val version = AppVersion.fromProperties(versionFile.get().asFile.readText()).name
        val arrayName = "whats_new_${version.replace('.', '_')}"
        val strings = whatsNewStrings.get().asFile.takeIf { it.exists() }?.readText().orEmpty()
        val releases = whatsNewReleases.get().asFile.takeIf { it.exists() }?.readText().orEmpty()
        val playNotes = playReleaseNotes.get().asFile.takeIf { it.exists() }?.readText().orEmpty()

        val items = Regex("""<string-array name="$arrayName">(.*?)</string-array>""", RegexOption.DOT_MATCHES_ALL)
            .find(strings)
            ?.groupValues
            ?.get(1)
            ?.let { body -> Regex("""<item>(.*?)</item>""").findAll(body).map { unescape(it.groupValues[1]) }.toList() }

        val problems = buildList {
            if (items == null) add("no <string-array name=\"$arrayName\"> in ${whatsNewStrings.get().asFile.name}")
            if ("version = \"$version\"" !in releases) add("$version isn't listed in WhatsNewReleases.kt")
            items?.filterNot { it in playNotes }?.forEach { add("Play release notes don't mention: \"$it\"") }
        }
        if (problems.isEmpty()) {
            logger.lifecycle("Release notes for $version: OK (${items?.size} highlights)")
        } else {
            logger.warn("WARNING: release notes for $version look incomplete:")
            problems.forEach { logger.warn("  - $it") }
        }
    }

    private fun unescape(text: String) = text
        .replace("\\'", "'")
        .replace("\\\"", "\"")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .trim()
}
