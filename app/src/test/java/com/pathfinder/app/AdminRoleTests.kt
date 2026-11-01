package com.pathfinder.app

import com.pathfinder.app.data.validation.PathwiseInputValidation
import org.junit.Assert.assertEquals
import org.junit.Test

class AdminRoleTests {
    @Test fun registrationOffersTheThreeSupportedAccountRoles() {
        assertEquals(listOf("Student", "Counsellor", "Admin"), PathwiseInputValidation.registrationRoles)
    }
}
