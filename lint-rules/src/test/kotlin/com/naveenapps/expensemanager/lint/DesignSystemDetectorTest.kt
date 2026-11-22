package com.naveenapps.expensemanager.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestMode
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

class DesignSystemDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector = DesignSystemDetector()

    override fun getIssues(): List<Issue> = DesignSystemDetector.ISSUES

    // These checks only read Kotlin imports and calls, so they don't need an Android SDK. Without
    // this, the tests fail wherever ANDROID_HOME isn't set (some IDE and terminal Gradle runs).
    private fun check(source: String) = lint()
        .allowMissingSdk()
        .files(kotlin(source).indented())
        .allowCompilationErrors()
        .testModes(TestMode.DEFAULT)
        .run()

    fun testMaterialImportIsFlagged() {
        check(
            """
            package test.pkg

            import androidx.compose.material3.Text
            import androidx.compose.material3.*
            """,
        ).expectErrorCount(2).expectContains("RawMaterialUsage")
    }

    fun testLibraryImportIsFlagged() {
        check(
            """
            package test.pkg

            import com.naveenapps.designsystem.theme.PrimaryButton
            """,
        ).expectErrorCount(1).expectContains("RawMaterialUsage")
    }

    fun testAllowedImportsAreClean() {
        check(
            """
            package test.pkg

            import androidx.compose.foundation.layout.Column
            import androidx.compose.material.icons.Icons
            import androidx.compose.material3.ExperimentalMaterial3Api
            import androidx.compose.ui.graphics.Color
            import androidx.compose.ui.unit.dp
            """,
        ).expectClean()
    }

    fun testTextStyleImportsAreFlagged() {
        check(
            """
            package test.pkg

            import androidx.compose.ui.text.font.FontWeight
            import androidx.compose.ui.unit.sp
            """,
        ).expectErrorCount(2).expectContains("HardcodedTextStyle")
    }

    fun testColorsAreFlagged() {
        check(
            """
            package test.pkg

            import androidx.compose.ui.res.colorResource

            val brand = Color(0xFF7E57C2)
            val text = Color.White
            """,
        ).expectErrorCount(3).expectContains("HardcodedColor")
    }

    fun testColorFromVariableIsClean() {
        check(
            """
            package test.pkg

            fun parse(value: Long) = Color(value)
            """,
        ).expectClean()
    }

    fun testShapeImportIsFlagged() {
        check(
            """
            package test.pkg

            import androidx.compose.foundation.shape.RoundedCornerShape
            """,
        ).expectErrorCount(1).expectContains("HardcodedShape")
    }
}
