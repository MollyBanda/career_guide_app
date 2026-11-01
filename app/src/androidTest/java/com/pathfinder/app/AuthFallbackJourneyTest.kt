package com.pathfinder.app

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Covers the real signup -> logout -> login journey when Firebase Auth is misconfigured. */
@RunWith(AndroidJUnit4::class)
class AuthFallbackJourneyTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    private fun hasText(value: String): Boolean = runCatching {
        composeRule.onNodeWithText(value, useUnmergedTree = true).assertExists()
    }.isSuccess

    @Test fun localAccountCanSignUpThenLogInAndReachHome() {
        if (hasText("Where do you want to go?")) {
            composeRule.onNodeWithContentDescription("Profile").performClick()
            composeRule.onNodeWithText("Log out").performScrollTo().performClick()
        }
        if (hasText("Welcome back! 👋")) composeRule.onNodeWithText("Back").performClick()
        if (hasText("Find the path that feels like you.")) {
            composeRule.onNodeWithText("Create my account").performClick()
        } else if (hasText("Welcome back! 👋")) {
            composeRule.onNodeWithText("Create Account").performClick()
        }

        val email = "codex-login-${System.currentTimeMillis()}@example.test"
        val password = "Pathwise123"
        val signUpFields = composeRule.onAllNodes(hasSetTextAction())
        signUpFields[0].performTextInput("Pathwise Login Test")
        signUpFields[1].performTextInput(email)
        signUpFields[2].performTextInput(password)
        signUpFields[3].performTextInput(password)
        composeRule.onNodeWithText("I agree to the Terms of Service & Privacy Policy.").performScrollTo().performClick()
        composeRule.onNodeWithText("Create Account", substring = false).performScrollTo().performClick()
        composeRule.waitUntil(35_000) { hasText("Where do you want to go?") }
        assertTrue("Signup should open Home", hasText("Where do you want to go?"))

        composeRule.onNodeWithContentDescription("Profile").performClick()
        composeRule.onNodeWithText("Log out").performScrollTo().performClick()
        composeRule.onNodeWithText("Welcome back! 👋").assertExists()
        val loginFields = composeRule.onAllNodes(hasSetTextAction())
        loginFields[0].performTextInput(email)
        loginFields[1].performTextInput(password)
        composeRule.onNodeWithText("Login").performClick()
        composeRule.waitUntil(35_000) { hasText("Where do you want to go?") }
        assertTrue("The same account should log in and return to Home", hasText("Where do you want to go?"))
    }
}
