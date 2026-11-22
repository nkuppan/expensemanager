package com.naveenapps.expensemanager.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestMode
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

class SharedLayerApiDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = SharedLayerApiDetector()

    override fun getIssues(): List<Issue> = SharedLayerApiDetector.ISSUES

    // These checks only read Kotlin imports and calls, so they don't need an Android SDK. Without
    // this, the tests fail wherever ANDROID_HOME isn't set (some IDE and terminal Gradle runs).
    private fun check(source: String) = lint()
        .allowMissingSdk()
        .files(kotlin(source).indented())
        .allowCompilationErrors()
        .testModes(TestMode.DEFAULT)
        .run()

    fun testJvmImportsInModelAreFlagged() {
        check(
            """
            package com.naveenapps.expensemanager.core.model

            import java.util.Date
            import java.math.BigDecimal
            import java.io.File
            """,
        ).expectWarningCount(3).expectContains("JvmApiInSharedLayer")
    }

    fun testAndroidImportsInRepositoryAreFlagged() {
        check(
            """
            package com.naveenapps.expensemanager.core.repository

            import android.net.Uri
            import com.google.gson.Gson
            """,
        ).expectWarningCount(2).expectContains("JvmApiInSharedLayer")
    }

    fun testSubPackagesOfDomainAreChecked() {
        check(
            """
            package com.naveenapps.expensemanager.core.domain.usecase.budget

            import java.time.ZoneId
            import androidx.datastore.preferences.core.Preferences
            """,
        ).expectWarningCount(2).expectContains("kotlinx.datetime")
    }

    fun testMultiplatformImportsAreClean() {
        check(
            """
            package com.naveenapps.expensemanager.core.domain

            import androidx.compose.runtime.Stable
            import androidx.compose.runtime.Immutable
            import kotlin.time.Instant
            import kotlinx.coroutines.flow.Flow
            import kotlinx.datetime.LocalDate
            import org.koin.dsl.module
            import com.naveenapps.expensemanager.core.model.Amount
            """,
        ).expectClean()
    }

    fun testOtherPackagesAreNotChecked() {
        check(
            """
            package com.naveenapps.expensemanager.core.data.repository

            import android.content.Context
            import java.io.File
            """,
        ).expectClean()
    }

    fun testSimilarlyNamedPackageIsNotChecked() {
        check(
            """
            package com.naveenapps.expensemanager.core.modelextras

            import java.util.Date
            """,
        ).expectClean()
    }
}
