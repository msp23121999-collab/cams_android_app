package com.example.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.features.auth.screens.LoginScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginFlowUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginFlow_entersCredentialsAndSubmits() {
        // 1. Arrange: Launch the LoginScreen
        composeTestRule.setContent {
            LoginScreen(
                role = "Student",
                onBack = {},
                onLoginSuccess = {}
            )
        }

        // 2. Act: Enter credentials
        composeTestRule.onNodeWithText("ID Number or Email").performTextInput("STD12345")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // 3. Act: Click Login Button
        // Note: In a real test with mocked ViewModel, we would verify the callback onLoginSuccess was triggered.
        // For scaffolding, we verify the button exists and can be clicked.
        composeTestRule.onNodeWithText("Login").performClick()
    }
}
