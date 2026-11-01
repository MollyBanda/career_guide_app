package com.pathfinder.app

import com.pathfinder.app.data.local.defaultOpportunities
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerCatalogueTests {
    @Test fun careerRecordsIncludeTheInformationShownOnCareerDetails() {
        val careers = defaultOpportunities().filter { it.category == "Careers" }
        assertTrue(careers.isNotEmpty())
        assertTrue(careers.all { it.overview.isNotBlank() && it.qualifications.isNotBlank() && it.skills.isNotBlank() && it.pathways.isNotBlank() })
    }

    @Test fun softwareDeveloperCareerIsAvailableForTheSubmittedScenario() {
        assertTrue(defaultOpportunities().any { it.title == "Software Developer" && it.category == "Careers" })
    }
}
