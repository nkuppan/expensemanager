package com.naveenapps.expensemanager.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UImportStatement

/**
 * Keeps the layers that will move to Kotlin Multiplatform first (core:model, core:repository,
 * core:domain) free of JVM- and Android-only APIs, so they can later compile for iOS as they are.
 * See the "Kotlin Multiplatform readiness" section in CLAUDE.md and the KMP groundwork checklist.
 *
 * Scoped by package rather than by module, so the same registry can run everywhere: files outside
 * the shared-layer packages are never reported.
 */
class SharedLayerApiDetector :
    Detector(),
    SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UImportStatement::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        val packageName = context.uastFile?.packageName ?: return null
        if (!isSharedLayerPackage(packageName)) return null
        return object : UElementHandler() {
            override fun visitImportStatement(node: UImportStatement) {
                val path = node.importReference?.asSourceString() ?: return
                if (!isPlatformOnly(path)) return
                context.report(
                    JVM_API_IN_SHARED_LAYER,
                    node,
                    context.getLocation(node),
                    "`$path` is JVM/Android-only and won't compile for iOS. ${replacementHint(path)}",
                )
            }
        }
    }

    companion object {
        private val IMPLEMENTATION = Implementation(SharedLayerApiDetector::class.java, Scope.JAVA_FILE_SCOPE)

        val JVM_API_IN_SHARED_LAYER: Issue = Issue.create(
            id = "JvmApiInSharedLayer",
            briefDescription = "JVM/Android-only API in a layer that will be shared with iOS",
            explanation = """
                core:model, core:repository and core:domain are the first modules to move to \
                Kotlin Multiplatform, so they must only use APIs that also exist on iOS: the Kotlin \
                standard library, kotlinx (coroutines, serialization, datetime), Koin and the Compose \
                stability annotations. Keep Android and JVM types (java.*, javax.*, android.*, \
                androidx.* except the allowed annotations, Gson, org.json, Firebase) in core:data or \
                the feature modules, behind an interface declared in core:repository.
                """,
            category = Category.CORRECTNESS,
            priority = 5,
            // A warning while the existing usages are migrated; raise to ERROR once a module is clean.
            severity = Severity.WARNING,
            implementation = IMPLEMENTATION,
        )

        val ISSUES: List<Issue> = listOf(JVM_API_IN_SHARED_LAYER)

        private val SHARED_LAYER_PACKAGES = listOf(
            "com.naveenapps.expensemanager.core.model",
            "com.naveenapps.expensemanager.core.repository",
            "com.naveenapps.expensemanager.core.domain",
        )

        private val PLATFORM_ONLY_PREFIXES = listOf(
            "java.",
            "javax.",
            "android.",
            "androidx.",
            "org.json.",
            "com.google.gson.",
            "com.google.firebase.",
            "com.google.android.",
        )

        /** androidx APIs that are published for iOS too and are fine in the shared layers. */
        private val ALLOWED = listOf(
            "androidx.compose.runtime.Stable",
            "androidx.compose.runtime.Immutable",
            "androidx.compose.runtime.StableMarker",
            "androidx.annotation.",
            "androidx.collection.",
        )

        internal fun isSharedLayerPackage(packageName: String): Boolean = SHARED_LAYER_PACKAGES.any { packageName == it || packageName.startsWith("$it.") }

        internal fun isPlatformOnly(importPath: String): Boolean = PLATFORM_ONLY_PREFIXES.any { importPath.startsWith(it) } &&
            ALLOWED.none { allowed ->
                if (allowed.endsWith(".")) importPath.startsWith(allowed) else importPath == allowed
            }

        private fun replacementHint(path: String): String = when {
            path.startsWith("java.util.Date") || path.startsWith("java.util.Calendar") ->
                "Use `kotlin.time.Instant` or `kotlinx.datetime.LocalDate`."

            path.startsWith("java.time.") -> "Use the matching `kotlinx.datetime` type."

            path.startsWith("java.util.UUID") -> "Use `kotlin.uuid.Uuid`."

            path.startsWith("java.util.Locale") -> "Take the locale from `LocaleRepository` instead."

            path.startsWith("java.util.concurrent.") -> "Use coroutines or `kotlin.time.Duration`."

            path.startsWith("java.math.") -> "Use the `Money` type (see the money migration plan)."

            path.startsWith("java.io.File") -> "Pass an absolute path as a `String`."

            path.startsWith("android.net.Uri") -> "Pass the Uri as a `String` (`uri.toString()`)."

            path.startsWith("com.google.gson.") || path.startsWith("org.json.") -> "Use kotlinx.serialization."

            else -> "Move it behind an interface in core:repository with the implementation in core:data."
        }
    }
}
