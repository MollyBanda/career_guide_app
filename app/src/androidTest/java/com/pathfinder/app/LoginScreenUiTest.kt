package com.pathfinder.app

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pathfinder.app.ui.screens.auth.SignInScreen
import com.pathfinder.app.ui.theme.PathwiseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun loginSubmitsCredentialsWithoutAskingForRole() {
        var submitted: List<String>? = null
        composeRule.setContent {
            PathwiseTheme {
                SignInScreen({}, {}, {}, { email, password, role -> submitted = listOf(email, password, role) })
            }
        }
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("counsellor@example.com")
        fields[1].performTextInput("Career123")
        composeRule.onNodeWithText("Student").assertDoesNotExist()
        composeRule.onNodeWithText("Counsellor").assertDoesNotExist()
        composeRule.onNodeWithText("Admin").assertDoesNotExist()
        composeRule.onNodeWithText("Data Science Explorer").assertDoesNotExist()
        composeRule.onNodeWithText("256-bit SSL encrypted & secure").assertDoesNotExist()
        composeRule.onNodeWithText("Welcome back!").assertExists()
        composeRule.onNodeWithText("Login").performClick()
        composeRule.runOnIdle { assertEquals(listOf("counsellor@example.com", "Career123", ""), submitted) }
    }

    @Test fun loginNavigationAndPasswordResetActionsReachTheirHandlers() {
        var createOpened = false
        var backOpened = false
        var resetEmail = ""
        composeRule.setContent {
            PathwiseTheme {
                SignInScreen({ backOpened = true }, { createOpened = true }, {}, { _, _, _ -> }, { resetEmail = it })
            }
        }
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("reset@example.com")
        composeRule.onNodeWithText("Forgot password?").performClick()
        composeRule.onNodeWithText("Create Account").performClick()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.runOnIdle {
            assertEquals("reset@example.com", resetEmail)
            assertTrue(createOpened)
            assertTrue(backOpened)
        }
    }
}
