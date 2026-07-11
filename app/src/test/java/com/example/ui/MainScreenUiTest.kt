package com.example.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Example UI/E2E Test using Roborazzi for Screenshot Verification.
 * Ensures the UI components render correctly.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "xxhdpi")
class MainScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `capture basic composable`() {
        composeTestRule.setContent {
            Text("Hello Enterprise UI")
        }
        
        composeTestRule
            .onRoot()
            .captureRoboImage()
    }
}
