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

    @Test fun requestedCareerProfilesAreCompleteAndGroupedIntoThreeCategories() {
        val careers = defaultOpportunities().filter { it.category=="Careers" && (it.id.startsWith("career-guide-") || it.id in setOf("career-cybersecurity","career-data","career-data-engineer")) }
        assertTrue("Expected 20 requested career profiles", careers.size == 20)
        assertTrue(careers.all { it.overview.length > 80 && it.courses.contains("Main responsibilities") && it.qualifications.isNotBlank() && it.skills.isNotBlank() && it.pathways.contains("Career progression") })
        assertTrue(careers.map { it.subtitle.substringBefore(" ·") }.toSet() == setOf("Technology", "Health Science", "Design"))
    }
}
