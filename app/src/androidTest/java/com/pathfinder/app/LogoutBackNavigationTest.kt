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

@RunWith(AndroidJUnit4::class)
class LogoutBackNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun hasText(text: String): Boolean = runCatching {
        composeRule.onNodeWithText(text, useUnmergedTree = true).assertExists()
    }.isSuccess

    private fun createTestAccountIfNeeded() {
        if (hasText("Where do you want to go?")) return

        if (hasText("Log out")) {
            composeRule.onNodeWithText("Log out").performScrollTo().performClick()
        }
        if (hasText("Welcome back! 👋")) {
            composeRule.onNodeWithText("Back").performClick()
        }
        if (hasText("Find the path that feels like you.")) {
            composeRule.onNodeWithText("Create my account").performClick()
        } else if (hasText("Welcome back! 👋")) {
            composeRule.onNodeWithText("Create Account").performClick()
        }

        val fields = composeRule.onAllNodes(hasSetTextAction())
        val email = "codex-ui-${System.currentTimeMillis()}@example.test"
        fields[0].performTextInput("Pathwise Test User")
        fields[1].performTextInput(email)
        fields[2].performTextInput("Pathwise123")
        fields[3].performTextInput("Pathwise123")
        composeRule.onNodeWithText("I agree to the Terms of Service & Privacy Policy.").performScrollTo().performClick()
        composeRule.onNodeWithText("Create Account").performScrollTo().performClick()
    }

    @Test
    fun CG_LOGOUT_002_afterLogoutAndroidBackDoesNotReturnToHome() {
        createTestAccountIfNeeded()
        composeRule.waitUntil(timeoutMillis = 35_000) {
            hasText("Where do you want to go?")
        }
        assertTrue("Account setup should reach the authenticated Home screen", hasText("Where do you want to go?"))
        composeRule.onNodeWithContentDescription("Profile").performClick()
        composeRule.onNodeWithText("Log out").performScrollTo().performClick()
        composeRule.onNodeWithText("Welcome back! 👋").assertExists()

        var activityFinishing = false
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
            activityFinishing = activity.isFinishing
        }

        if (!activityFinishing) {
            assertTrue("After logout, back must not restore the authenticated Home screen", !hasText("Where do you want to go?"))
        } else {
            assertTrue("The activity should close when system Back exits the signed-out app", activityFinishing)
        }
    }
}
