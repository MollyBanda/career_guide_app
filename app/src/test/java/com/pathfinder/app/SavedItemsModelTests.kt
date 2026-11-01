package com.pathfinder.app

import com.pathfinder.app.data.model.SavedItemEntity
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedItemsModelTests {
    @Test fun savedItemIsScopedToItsUserAndOpportunity() {
        val aliceBookmark = SavedItemEntity("alice@example.com", "career-software", savedAt = 10L)
        val sameBookmark = SavedItemEntity("alice@example.com", "career-software", savedAt = 10L)
        val otherUserBookmark = SavedItemEntity("bob@example.com", "career-software", savedAt = 10L)
        val otherOpportunity = SavedItemEntity("alice@example.com", "career-ux", savedAt = 10L)

        assertEquals(aliceBookmark, sameBookmark)
        assertNotEquals(aliceBookmark, otherUserBookmark)
        assertNotEquals(aliceBookmark, otherOpportunity)
    }
}
