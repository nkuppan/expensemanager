package com.naveenapps.buildsrc.release

import com.naveenapps.buildsrc.extensions.AppVersion
import com.naveenapps.buildsrc.extensions.VERSION_KEY
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * Raises the version in version.properties: patch by default (1.4.27 → 1.4.28), or
 * `-Pbump=minor` (1.5.0) / `-Pbump=major` (2.0.0). Only the versionName line is rewritten, so
 * the file's comments stay. It has no outputs on purpose: it must run every time it's asked to.
 */
abstract class BumpVersionTask : DefaultTask() {

    @get:Internal
    abstract val versionFile: RegularFileProperty

    @get:Input
    abstract val part: Property<String>

    @TaskAction
    fun bump() {
        val file = versionFile.get().asFile
        val text = file.readText()
        val current = AppVersion.fromProperties(text)
        val next = current.bump(part.get())
        val line = Regex("""(?m)^\s*${Regex.escape(VERSION_KEY)}\s*[=:].*$""")
        file.writeText(text.replace(line, "$VERSION_KEY=${next.name}"))
        logger.lifecycle("Version ${current.name} (${current.code}) → ${next.name} (${next.code})")
    }
}
