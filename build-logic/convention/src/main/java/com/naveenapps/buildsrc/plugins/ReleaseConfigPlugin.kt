package com.naveenapps.buildsrc.plugins

import com.naveenapps.buildsrc.extensions.VERSION_FILE
import com.naveenapps.buildsrc.release.BumpVersionTask
import com.naveenapps.buildsrc.release.CheckReleaseNotesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Release helpers for the root project:
 * - `./gradlew bumpVersion [-Pbump=patch|minor|major]` raises version.properties, then runs
 * - `./gradlew checkReleaseNotes`, which warns when the new version has no what's-new notes.
 */
class ReleaseConfigPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val versionFile = layout.projectDirectory.file(VERSION_FILE)

            val checkReleaseNotes = tasks.register<CheckReleaseNotesTask>("checkReleaseNotes") {
                group = "release"
                description = "Warns when the current version has no what's-new or Play release notes."
                this.versionFile.set(versionFile)
                whatsNewStrings.set(layout.projectDirectory.file("feature/dashboard/src/main/res/values/strings.xml"))
                whatsNewReleases.set(
                    layout.projectDirectory.file(
                        "feature/dashboard/src/main/kotlin/com/naveenapps/expensemanager/feature/dashboard/" +
                            "notifications/WhatsNewReleases.kt",
                    ),
                )
                playReleaseNotes.set(layout.projectDirectory.file("app/src/main/play/release-notes/en-US/default.txt"))
            }

            tasks.register<BumpVersionTask>("bumpVersion") {
                group = "release"
                description = "Raises the app version in $VERSION_FILE (-Pbump=patch|minor|major, default patch)."
                this.versionFile.set(versionFile)
                part.set(providers.gradleProperty("bump").orElse("patch"))
                finalizedBy(checkReleaseNotes)
            }
        }
    }
}
