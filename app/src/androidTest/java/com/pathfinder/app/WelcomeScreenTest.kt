package com.pathfinder.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pathfinder.app.ui.screens.welcome.WelcomeScreen
import com.pathfinder.app.ui.theme.PathwiseTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun welcomeScreenDisplaysMainActions() {
        composeRule.setContent {
            PathwiseTheme {
                WelcomeScreen(create = {}, signIn = {})
            }
        }

        composeRule.onNodeWithText("Find the path that feels like you.").assertIsDisplayed()
        composeRule.onNodeWithText("Create my account").assertIsDisplayed()
        composeRule.onNodeWithText("I already have an account").assertIsDisplayed()
    }

    @Test
    fun welcomeActionsInvokeTheirCallbacks() {
        var createTapped = false
        var signInTapped = false
        composeRule.setContent {
            PathwiseTheme {
                WelcomeScreen(
                    create = { createTapped = true },
                    signIn = { signInTapped = true }
                )
            }
        }

        composeRule.onNodeWithText("Create my account").performClick()
        composeRule.onNodeWithText("I already have an account").performClick()

        composeRule.runOnIdle {
            assertTrue("Create-account action did not run", createTapped)
            assertTrue("Sign-in action did not run", signInTapped)
        }
    }
}
