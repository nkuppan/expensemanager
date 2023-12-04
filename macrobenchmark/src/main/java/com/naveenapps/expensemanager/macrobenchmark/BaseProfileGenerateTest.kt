package com.naveenapps.expensemanager.macrobenchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class BaseProfileGenerateTest {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startup() = baselineProfileRule.collect(
        packageName = TARGET_PACKAGE,
        profileBlock = {
            startActivityAndWait()
            device.waitForIdle()
        },
    )
}
