package com.pathfinder.app

import com.pathfinder.app.data.validation.PathwiseInputValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileValidationTests {
    @Test fun emptyOrWhitespaceNameIsRejected() {
        assertEquals("Please fill all fields.", PathwiseInputValidation.profileNameError("  "))
    }

    @Test fun validNameCanBeSaved() {
        assertNull(PathwiseInputValidation.profileNameError("Chikondi Banda"))
    }
}
