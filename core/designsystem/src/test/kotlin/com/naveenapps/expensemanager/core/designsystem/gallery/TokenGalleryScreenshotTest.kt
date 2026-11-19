package com.naveenapps.expensemanager.core.designsystem.gallery

import com.github.takahirom.roborazzi.captureRoboImage
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot of the design tokens in light and dark. Record with
 * `./gradlew :core:designsystem:testDebugUnitTest -Proborazzi.test.record=true`; CI verifies.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TokenGalleryScreenshotTest {

    @Test
    fun tokensLight() {
        captureRoboImage(SCREENSHOT_DIR + "token_gallery_light.png") {
            ExpenseManagerTheme(isDarkTheme = false) { TokenGallery() }
        }
    }

    @Test
    fun tokensDark() {
        captureRoboImage(SCREENSHOT_DIR + "token_gallery_dark.png") {
            ExpenseManagerTheme(isDarkTheme = true) { TokenGallery() }
        }
    }

    private companion object {
        const val SCREENSHOT_DIR = "src/test/screenshots/"
    }
}
