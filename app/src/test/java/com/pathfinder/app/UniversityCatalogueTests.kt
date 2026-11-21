package com.pathfinder.app

import com.pathfinder.app.data.local.defaultOpportunities
import com.pathfinder.app.data.local.defaultUniversityProgrammes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversityCatalogueTests {
    @Test fun universityRecordsHaveNamesAndProviders() {
        val universities = defaultOpportunities().filter { it.category == "Universities" && it.id.startsWith("uni-") }
        assertTrue(universities.isNotEmpty())
        assertTrue(universities.all { it.title.isNotBlank() && it.provider.isNotBlank() })
    }

    @Test fun computerScienceProgrammeIncludesEntryRequirements() {
        val programme = defaultUniversityProgrammes().single { it.id == "programme-unima-bsc-computer-science" }
        assertTrue(programme.entryRequirements.contains("Mathematics"))
        assertTrue(programme.entryRequirements.contains("English"))
        assertTrue(programme.entryRequirements.contains("Physics"))
    }

    @Test fun requestedInstitutionSetContainsThirtyStableRecordsAndTheCurrentHealthUniversityName() {
        val requested = defaultOpportunities().filter { it.category=="Universities" && (it.id=="uni-malawi" || it.id.startsWith("uni-expanded-")) }
        assertTrue("Expected all 30 requested institution records", requested.size == 30)
        assertTrue(requested.any { it.title.startsWith("Kamuzu University of Health Sciences") && it.provider.contains("former College of Medicine") })
        assertTrue(requested.all { (it.applicationUrl.startsWith("https://") || it.title == "Daeyang University") && it.provider.contains("Checked 2026-11-21") })
    }

    @Test fun programmesAreAssociatedWithTheUniversityThatOffersThem() {
        val programmes = defaultUniversityProgrammes()
        val unimaId = defaultOpportunities().single { it.title == "University of Malawi" && it.category == "Universities" }.id
        val mitId = defaultOpportunities().single { it.title.startsWith("Massachusetts Institute") && it.category == "Universities" }.id
        assertEquals(1,programmes.count { it.universityId == unimaId })
        assertEquals(1,programmes.count { it.universityId == mitId })
        assertTrue(programmes.filter { it.universityId == unimaId }.all { it.programmeName.contains("Computer Science") && it.sourceUrl.contains("unima.ac.mw") })
        assertTrue(programmes.filter { it.universityId == mitId }.all { it.programmeName.contains("Course 6-3") && it.sourceUrl.contains("catalog.mit.edu") })
        assertTrue(programmes.none { it.universityId == unimaId && it.programmeName.contains("Course 6-3") })
        assertTrue(programmes.none { it.universityId == mitId && it.programmeName.contains("University of Malawi") })
    }
}
