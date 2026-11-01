package com.pathfinder.app

import com.pathfinder.app.data.local.defaultOpportunities
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingInternshipCatalogueTests {
    @Test fun trainingAndInternshipRecordsHaveDescriptionsAndTitles() {
        val items = defaultOpportunities().filter { it.category == "Training" || it.category == "Internships" }
        assertTrue(items.any { it.category == "Training" })
        assertTrue(items.any { it.category == "Internships" })
        assertTrue(items.all { it.title.isNotBlank() && it.overview.isNotBlank() })
    }

    @Test fun internshipRecordsIncludeApplicationRequirements() {
        val internships = defaultOpportunities().filter { it.category == "Internships" }
        assertTrue(internships.isNotEmpty())
        assertTrue(internships.all { it.requirements.isNotBlank() })
    }
}
