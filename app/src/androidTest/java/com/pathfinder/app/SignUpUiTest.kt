package com.pathfinder.app

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pathfinder.app.ui.screens.auth.SignUpScreen
import com.pathfinder.app.ui.theme.PathwiseTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun counsellorCanBeSelectedForRegistration() = assertRoleCanBeSelected("Counsellor")

    @Test fun adminCanBeSelectedForRegistration() = assertRoleCanBeSelected("Admin")

    private fun assertRoleCanBeSelected(role: String) {
        var selectedRole = ""
        composeRule.setContent {
            PathwiseTheme { SignUpScreen({}, {}, {}, { _, _, _, selected, education -> selectedRole = "$selected:$education" }) }
        }
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("Pathwise Test")
        fields[1].performTextInput("${role.lowercase()}@example.com")
        fields[2].performTextInput("Career123")
        fields[3].performTextInput("Career123")
        composeRule.onNodeWithText(role).performClick()
        composeRule.onNodeWithText("Where are you right now?  Helps tailor paths").assertDoesNotExist()
        composeRule.onNodeWithText("I agree to the Terms of Service & Privacy Policy.").performScrollTo().performClick()
        composeRule.onNodeWithText("Create Account", substring = false).performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals("$role:Exploring", selectedRole) }
    }

    @Test fun studentEducationLevelCanBeSelectedAndIsSubmitted() {
        var registration = ""
        composeRule.setContent {
            PathwiseTheme { SignUpScreen({}, {}, {}, { _, _, _, role, education -> registration = "$role:$education" }) }
        }
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("Pathwise Test")
        fields[1].performTextInput("student-level@example.com")
        fields[2].performTextInput("Career123")
        fields[3].performTextInput("Career123")
        composeRule.onNodeWithText("Admin").performClick()
        composeRule.onNodeWithText("Where are you right now?  Helps tailor paths").assertDoesNotExist()
        composeRule.onNodeWithText("Student").performClick()
        composeRule.onNodeWithText("Where are you right now?  Helps tailor paths").assertExists()
        composeRule.onNodeWithText("High School").performScrollTo().performClick()
        composeRule.onNodeWithText("I agree to the Terms of Service & Privacy Policy.").performScrollTo().performClick()
        composeRule.onNodeWithText("Create Account", substring = false).performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals("Student:High School", registration) }
    }

    @Test fun passwordHintOnlyShowsWhenPasswordIsMissingOrWeak() {
        composeRule.setContent { PathwiseTheme { SignUpScreen({}, {}, {}, { _, _, _, _, _ -> }) } }
        val passwordField = composeRule.onAllNodes(hasSetTextAction())[2]
        composeRule.onNodeWithText("WEAK PASSWORD").assertDoesNotExist()
        composeRule.onNodeWithText("STRONG PASSWORD").assertDoesNotExist()
        passwordField.performTextInput("short")
        composeRule.onNodeWithText("WEAK PASSWORD").assertExists()
        passwordField.performTextClearance()
        passwordField.performTextInput("Career123!")
        composeRule.onNodeWithText("WEAK PASSWORD").assertDoesNotExist()
        composeRule.onNodeWithText("STRONG PASSWORD").assertExists()
    }

    @Test fun termsConsentIsRequiredBeforeRegistrationSubmission() {
        var submitted = false
        var error = ""
        composeRule.setContent {
            PathwiseTheme {
                SignUpScreen({}, {}, { error = it }, { _, _, _, _, _ -> submitted = true })
            }
        }
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("Pathwise Test")
        fields[1].performTextInput("student@example.com")
        fields[2].performTextInput("Career123")
        fields[3].performTextInput("Career123")
        composeRule.onNodeWithText("Create Account", substring = false).performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals("Please agree to the Terms of Service and Privacy Policy.", error)
            assertEquals(false, submitted)
        }
    }
}
