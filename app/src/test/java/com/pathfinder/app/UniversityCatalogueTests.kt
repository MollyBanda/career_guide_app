package com.pathfinder.app

import com.pathfinder.app.data.local.defaultOpportunities
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversityCatalogueTests {
    @Test fun universityRecordsHaveNamesAndProviders() {
        val universities = defaultOpportunities().filter { it.category == "Universities" && it.id.startsWith("uni-") }
        assertTrue(universities.isNotEmpty())
        assertTrue(universities.all { it.title.isNotBlank() && it.provider.isNotBlank() })
    }

    @Test fun computerScienceProgrammeIncludesEntryRequirements() {
        val programme = defaultOpportunities().single { it.id == "degree-bsc-cs" }
        assertTrue(programme.requirements.contains("Mathematics"))
        assertTrue(programme.requirements.contains("English"))
        assertTrue(programme.requirements.contains("Physics"))
    }
}
