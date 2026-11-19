package com.naveenapps.expensemanager.core.designsystem.gallery

import com.github.takahirom.roborazzi.captureRoboImage
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot of every Em* component in light and dark. A tall device so the whole gallery fits.
 * Record with `./gradlew :core:designsystem:testDebugUnitTest -Proborazzi.test.record=true`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h3600dp-xxhdpi")
class ComponentGalleryScreenshotTest {

    @Test
    fun componentsLight() {
        captureRoboImage("src/test/screenshots/component_gallery_light.png") {
            ExpenseManagerTheme(isDarkTheme = false) { ComponentGallery(showIndeterminate = false) }
        }
    }

    @Test
    fun componentsDark() {
        captureRoboImage("src/test/screenshots/component_gallery_dark.png") {
            ExpenseManagerTheme(isDarkTheme = true) { ComponentGallery(showIndeterminate = false) }
        }
    }

    /** Largest common accessibility font size: rows and buttons must not clip. */
    @Test
    @Config(fontScale = 1.5f)
    fun componentsLargeFont() {
        captureRoboImage("src/test/screenshots/component_gallery_font_1_5.png") {
            ExpenseManagerTheme(isDarkTheme = false) { ComponentGallery(showIndeterminate = false) }
        }
    }
}
