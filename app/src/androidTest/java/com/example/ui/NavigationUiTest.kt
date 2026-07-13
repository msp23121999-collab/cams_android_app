package com.example.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.core.navigation.BottomNavigationBar
import com.example.core.navigation.AppRoutes
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomNavigation_studentRole_displaysCorrectTabs() {
        composeTestRule.setContent {
            BottomNavigationBar(
                currentRoute = AppRoutes.STUDENT_DASHBOARD,
                onNavigate = {},
                role = "STUDENT"
            )
        }

        // Verify Student Tabs
        composeTestRule.onNodeWithText("Dashboard").assertExists()
        composeTestRule.onNodeWithText("Campus Life").assertExists()
        composeTestRule.onNodeWithText("Academics").assertExists()
    }
}
