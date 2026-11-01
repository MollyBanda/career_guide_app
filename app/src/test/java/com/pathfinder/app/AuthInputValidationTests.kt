package com.pathfinder.app

import com.pathfinder.app.data.validation.PathwiseInputValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInputValidationTests {
    @Test fun invalidEmailIsRejected() {
        assertEquals("Enter a valid email address.", PathwiseInputValidation.registrationError("Learner", "bad-email", "Career123", "Career123", true))
    }

    @Test fun fiveCharacterPasswordIsRejectedAndSixCharactersAreAccepted() {
        assertEquals("Password must be at least 6 characters.", PathwiseInputValidation.registrationError("Learner", "learner@example.com", "Ab123", "Ab123", true))
        assertNull(PathwiseInputValidation.registrationError("Learner", "learner@example.com", "Ab1234", "Ab1234", true))
    }

    @Test fun blankFieldsAndMismatchedPasswordsAreRejected() {
        assertEquals("Please fill all fields.", PathwiseInputValidation.registrationError(" ", "learner@example.com", "Career123", "Career123", true))
        assertEquals("Passwords do not match.", PathwiseInputValidation.registrationError("Learner", "learner@example.com", "Career123", "Other123", true))
    }

    @Test fun termsMustBeAcceptedAndValidInputPasses() {
        assertEquals("Please agree to the Terms of Service and Privacy Policy.", PathwiseInputValidation.registrationError("Learner", "learner@example.com", "Career123", "Career123", false))
        assertNull(PathwiseInputValidation.registrationError("Learner", "learner@example.com", "Career123", "Career123", true))
    }

    @Test fun loginRequiresValidEmailAndNonBlankPassword() {
        assertEquals("Enter a valid email address and password.", PathwiseInputValidation.loginError("bad-email", "Career123"))
        assertEquals("Enter a valid email address and password.", PathwiseInputValidation.loginError("learner@example.com", " "))
        assertNull(PathwiseInputValidation.loginError("learner@example.com", "Career123"))
    }
}
