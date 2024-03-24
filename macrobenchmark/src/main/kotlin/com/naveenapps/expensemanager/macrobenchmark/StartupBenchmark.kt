package com.naveenapps.expensemanager.macrobenchmark

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import org.junit.Rule
import org.junit.Test

const val TARGET_PACKAGE = "com.naveenapps.expensemanager"

class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @RequiresApi(Build.VERSION_CODES.N)
    @Test
    fun startupWithNoCompilation() = startup(CompilationMode.None())

    @RequiresApi(Build.VERSION_CODES.N)
    @Test
    fun startupWithBaselineProfileCompilation() =
        startup(CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require))

    private fun startup(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        compilationMode = compilationMode,
        setupBlock = {
            // Press home button before each run to ensure the starting activity isn't visible.
            pressHome()
        },
    ) {
        // starts default launch activity
        startActivityAndWait()
    }
}
