package com.pathfinder.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pathfinder.app.ui.screens.profile.ProfileScreen
import com.pathfinder.app.ui.theme.PathwiseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTests {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun CG_PROFILE_001_profileShowsRegisteredIdentity() {
        composeRule.setContent {
            PathwiseTheme {
                ProfileScreen(PaddingValues(), {}, {}, "Test Learner", "learner@example.com", { _, _ -> }, {}, { _, _ -> }, null)
            }
        }
        composeRule.onNodeWithText("Test Learner").assertExists()
        composeRule.onNodeWithText("learner@example.com").assertExists()
    }

    @Test
    fun CG_PROFILE_002_validProfileEditCallsUpdateHandler() {
        var updatedName = ""
        var updatedEmail = ""
        composeRule.setContent {
            PathwiseTheme {
                ProfileScreen(PaddingValues(), {}, {}, "Test Learner", "learner@example.com", { _, _ -> }, {},
                    { name, email -> updatedName = name; updatedEmail = email }, null)
            }
        }
        composeRule.onNodeWithText("Edit").performClick()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextClearance()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("Updated Learner")
        composeRule.onNodeWithText("Save").performClick()
        composeRule.runOnIdle {
            assertEquals("Updated Learner", updatedName)
            assertEquals("learner@example.com", updatedEmail)
        }
    }

    @Test
    fun CG_PROFILE_003_emptyProfileNameShowsValidation() {
        var message = ""
        var updateCalled = false
        composeRule.setContent {
            PathwiseTheme {
                ProfileScreen(PaddingValues(), {}, { message = it }, "Test Learner", "learner@example.com", { _, _ -> }, {},
                    { _, _ -> updateCalled = true }, null)
            }
        }
        composeRule.onNodeWithText("Edit").performClick()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextClearance()
        composeRule.onNodeWithText("Save").performClick()
        composeRule.runOnIdle {
            assertEquals("Please fill all fields.", message)
            assertTrue("Invalid profile should not be saved", !updateCalled)
        }
    }

    @Test
    fun profileCanAddEducationAndCareerPreferences() {
        composeRule.setContent {
            PathwiseTheme {
                ProfileScreen(PaddingValues(), {}, {}, "Test Learner", "learner@example.com", { _, _ -> }, {}, { _, _ -> }, null)
            }
        }
        composeRule.onNodeWithText("Education & skills").performScrollTo().performClick()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("BSc Computing; Kotlin")
        composeRule.onNodeWithText("Save").performClick()
        composeRule.onNodeWithText("BSc Computing; Kotlin").assertExists()

        composeRule.onNodeWithText("Career preferences").performScrollTo().performClick()
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("UX Designer; Lilongwe")
        composeRule.onNodeWithText("Save").performClick()
        composeRule.onNodeWithText("UX Designer; Lilongwe").assertExists()
    }

    @Test
    fun CG_LOGOUT_001_logoutButtonCallsLogoutHandler() {
        var logoutCalled = false
        composeRule.setContent {
            PathwiseTheme {
                ProfileScreen(PaddingValues(), { logoutCalled = true }, {}, "Test Learner", "learner@example.com", { _, _ -> }, {}, { _, _ -> }, null)
            }
        }
        composeRule.onNodeWithText("Log out").performClick()
        composeRule.runOnIdle { assertTrue(logoutCalled) }
    }
}
