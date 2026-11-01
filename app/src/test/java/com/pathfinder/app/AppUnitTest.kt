package com.pathfinder.app

import com.pathfinder.app.data.local.defaultOpportunities
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUnitTest {
    @Test fun bundledCatalogueIsNonEmptyAndEveryItemHasIdentityAndTitle() {
        val items = defaultOpportunities()
        assertTrue(items.isNotEmpty())
        assertTrue(items.all { it.id.isNotBlank() && it.title.isNotBlank() })
    }

    @Test fun bundledCatalogueHasNoDuplicateIdsOrCategoryTitlePairs() {
        val items = defaultOpportunities()
        assertFalse(items.map { it.id }.hasDuplicates())
        assertFalse(items.map { it.category to it.title }.hasDuplicates())
    }

    private fun <T> List<T>.hasDuplicates() = size != toSet().size
}
