package com.pathfinder.app

import com.pathfinder.app.data.validation.PathwiseInputValidation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssessmentValidationTests {
    @Test fun fiveNonBlankAnswersAreRequiredBeforeSaving() {
        assertTrue(PathwiseInputValidation.hasCompleteAssessment(listOf("a", "b", "c", "d", "e")))
        assertFalse(PathwiseInputValidation.hasCompleteAssessment(listOf("a", "b", "c", "d")))
        assertFalse(PathwiseInputValidation.hasCompleteAssessment(listOf("a", "b", "c", "d", " ")))
        assertFalse(PathwiseInputValidation.hasCompleteAssessment(listOf("a", "b", "c", "d", "e", "f")))
    }
}
