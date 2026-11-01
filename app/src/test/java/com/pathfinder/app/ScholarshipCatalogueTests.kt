package com.pathfinder.app

import com.pathfinder.app.data.local.defaultOpportunities
import org.junit.Assert.assertTrue
import org.junit.Test

class ScholarshipCatalogueTests {
    @Test fun scholarshipRecordsHaveTitlesAndEligibilityDetails() {
        val scholarships = defaultOpportunities().filter { it.category == "Scholarships" }
        assertTrue(scholarships.isNotEmpty())
        assertTrue(scholarships.all { it.title.isNotBlank() && it.requirements.isNotBlank() })
    }

    @Test fun commonwealthScholarshipHasDeadlineAndApplicationSource() {
        val item = defaultOpportunities().single { it.id == "scholarship-commonwealth" }
        assertTrue(item.deadline.isNotBlank())
        assertTrue(item.applicationUrl.startsWith("https://"))
    }
}
