package com.pathfinder.app

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pathfinder.app.ui.screens.auth.SignInScreen
import com.pathfinder.app.ui.screens.auth.SignUpScreen
import com.pathfinder.app.ui.theme.PathwiseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun registrationScreen(notify: (String) -> Unit = {}, onAuthenticated: (String, String, String, String) -> Unit = { _, _, _, _ -> }) {
        composeRule.setContent {
            PathwiseTheme {
                SignUpScreen(back = {}, signIn = {}, notify = notify, onAuthenticated = onAuthenticated)
            }
        }
    }

    private fun fillRegistration(name: String, email: String, password: String, confirmation: String) {
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput(name)
        fields[1].performTextInput(email)
        fields[2].performTextInput(password)
        fields[3].performTextInput(confirmation)
    }

    @Test
    fun CG_REG_001_validRegistration_forwardsAccountDetails() {
        var submitted: List<String>? = null
        registrationScreen(onAuthenticated = { name, email, password, role -> submitted = listOf(name, email, password, role) })
        val email = "student-${System.currentTimeMillis()}@example.com"
        fillRegistration("Test Student", email, "Career123", "Career123")
        composeRule.onNodeWithText("Admin").performScrollTo().performClick()
        composeRule.onNodeWithText("I agree to the Terms of Service & Privacy Policy.").performScrollTo().performClick()
        composeRule.onNodeWithText("Create Account", substring = false).performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(listOf("Test Student", email, "Career123", "Admin"), submitted)
        }
    }

    @Test
    fun CG_REG_003_emptyRegistration_showsValidation() {
        var message = ""
        registrationScreen(notify = { message = it })
        composeRule.onNodeWithText("Create Account", substring = false).performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals("Please fill all fields.", message) }
    }

    @Test
    fun CG_REG_004_invalidEmail_showsValidation() {
        var message = ""
        registrationScreen(notify = { message = it })
        fillRegistration("Test Student", "not-an-email", "Career123", "Career123")
        composeRule.onNodeWithText("Create Account", substring = false).performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals("Enter a valid email address.", message) }
    }

    @Test
    fun CG_REG_005_shortPassword_showsValidation() {
        var message = ""
        registrationScreen(notify = { message = it })
        fillRegistration("Test Student", "student@example.com", "short", "short")
        composeRule.onNodeWithText("I agree to the Terms of Service & Privacy Policy.").performScrollTo().performClick()
        composeRule.onNodeWithText("Create Account").performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals("Password must be at least 6 characters.", message) }
    }

    @Test
    fun CG_REG_006_mismatchedPasswords_showsValidation() {
        var message = ""
        registrationScreen(notify = { message = it })
        fillRegistration("Test Student", "student@example.com", "Career123", "Different123")
        composeRule.onNodeWithText("I agree to the Terms of Service & Privacy Policy.").performScrollTo().performClick()
        composeRule.onNodeWithText("Create Account").performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals("Passwords do not match.", message) }
    }

    @Test
    fun cgLoginForm_showsWelcomeAndRequiredFields() {
        var message = ""
        composeRule.setContent {
            PathwiseTheme { SignInScreen(back = {}, create = {}, notify = { message = it }, onLogin = { _, _, _ -> }) }
        }
        composeRule.onNodeWithText("Welcome back! 👋").assertExists()
        composeRule.onNodeWithText("Login").performClick()
        composeRule.runOnIdle { assertEquals("Enter a valid email address and password.", message) }
    }

    @Test
    fun CG_LOGIN_001_validLoginForwardsCredentialsAndRole() {
        var submitted: Triple<String, String, String>? = null
        composeRule.setContent {
            PathwiseTheme { SignInScreen(back = {}, create = {}, notify = {}, onLogin = { email, password, role -> submitted = Triple(email, password, role) }) }
        }
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("student@example.com")
        fields[1].performTextInput("Career123")
        composeRule.onNodeWithText("Login").performClick()
        composeRule.runOnIdle { assertEquals(Triple("student@example.com", "Career123", "Student"), submitted) }
    }

    @Test
    fun CG_REG_002_duplicateRegistrationAttemptForwardsToHandler() {
        var attemptedEmail = ""
        var message = ""
        registrationScreen(
            notify = { message = it },
            onAuthenticated = { _, email, _, _ ->
                attemptedEmail = email
                message = "Email already registered."
            }
        )
        fillRegistration("Test Student", "existing@example.com", "Career123", "Career123")
        composeRule.onNodeWithText("I agree to the Terms of Service & Privacy Policy.").performScrollTo().performClick()
        composeRule.onNodeWithText("Create Account").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals("existing@example.com", attemptedEmail)
            assertEquals("Email already registered.", message)
        }
    }

    @Test
    fun CG_LOGIN_002_wrongPasswordAttemptIsSubmittedToAuthHandler() {
        var attemptedLogin = false
        composeRule.setContent {
            PathwiseTheme {
                SignInScreen(back = {}, create = {}, notify = {}, onLogin = { _, _, _ -> attemptedLogin = true })
            }
        }
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("missing@example.com")
        fields[1].performTextInput("wrong-password")
        composeRule.onNodeWithText("Login").performClick()
        composeRule.onNodeWithText("Welcome back! 👋").assertExists()
        composeRule.runOnIdle { assertTrue("Credentials should be submitted for repository authentication", attemptedLogin) }
    }

    @Test
    fun CG_LOGIN_003_unregisteredEmailAttemptIsSubmittedToAuthHandler() {
        var attemptedEmail = ""
        composeRule.setContent {
            PathwiseTheme {
                SignInScreen(back = {}, create = {}, notify = {}, onLogin = { email, _, _ -> attemptedEmail = email })
            }
        }
        val fields = composeRule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("unregistered-${System.currentTimeMillis()}@example.com")
        fields[1].performTextInput("Career123")
        composeRule.onNodeWithText("Login").performClick()
        composeRule.runOnIdle { assertTrue(attemptedEmail.startsWith("unregistered-")) }
    }

    @Test
    fun forgotPassword_withEmailCallsResetHandler() {
        var requestedEmail = ""
        composeRule.setContent {
            PathwiseTheme {
                SignInScreen(back = {}, create = {}, notify = {}, onLogin = { _, _, _ -> },
                    onForgotPassword = { requestedEmail = it })
            }
        }
        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("reset@example.com")
        composeRule.onNodeWithText("Forgot password?").performClick()
        composeRule.runOnIdle { assertEquals("reset@example.com", requestedEmail) }
    }
}
