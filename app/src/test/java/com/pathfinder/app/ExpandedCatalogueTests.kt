package com.pathfinder.app

import com.pathfinder.app.data.local.defaultOpportunities
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpandedCatalogueTests {
    @Test fun requestedScholarshipInternshipAndTrainingRecordsArePresentWithStableIdsAndSources() {
        val all = defaultOpportunities()
        val scholarships = all.filter { it.id.startsWith("scholarship-guide-") }
        val internships = all.filter { it.id.startsWith("internship-guide-") }
        val training = all.filter { it.id.startsWith("training-guide-") }
        assertEquals(10, scholarships.size)
        assertEquals(6, internships.size)
        assertEquals(9, training.size)
        assertTrue((scholarships + internships + training).all { it.id.isNotBlank() && it.provider.contains("Last checked: 2026-11-21") || it.provider.contains("Checked 2026-11-21") })
    }

    @Test fun applicationCyclesAreNotMarkedOpenWithoutAnOfficialCurrentCall() {
        val all = defaultOpportunities()
        assertTrue(all.single { it.id == "scholarship-guide-chevening-scholarship" }.deadline.contains("closed", true))
        assertTrue(all.single { it.id == "scholarship-guide-commonwealth-scholarship" }.deadline.contains("closed", true))
        assertTrue(all.filter { it.id.startsWith("internship-guide-") }.all { it.deadline.contains("No current vacancy") })
    }
}
