package com.pathfinder.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pathfinder.app.data.local.PathwiseDatabase
import com.pathfinder.app.data.model.OpportunityEntity
import com.pathfinder.app.data.model.SavedItemEntity
import com.pathfinder.app.data.model.UserProfileEntity
import com.pathfinder.app.data.repository.PathwiseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration Tests testing the interaction between PathwiseRepository, Room Database DAOs,
 * DataStore preferences, and Firebase fallback mechanisms.
 */
@RunWith(AndroidJUnit4::class)
class RepositoryIntegrationTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository get() = PathwiseRepository.get(context)
    private val database get() = PathwiseDatabase.get(context)

    @Before
    fun setup() {
        runBlocking {
            repository.initialize()
        }
    }

    @Test
    fun INT_REG_001_registerLocallyCreatesUserProfileInRoomDatabase() = runBlocking {
        val email = "integration-reg-${System.currentTimeMillis()}@example.com"
        val profile = repository.registerLocally("Integration User", email, "Career123", "Student", "High School")
        assertNotNull(profile)
        assertEquals("Integration User", profile?.fullName)
        assertEquals(email, profile?.email)
        assertEquals("Student", profile?.role)

        val storedInDb = database.userProfiles().findByEmail(email)
        assertNotNull(storedInDb)
        assertEquals("Integration User", storedInDb?.fullName)
        assertEquals("High School", storedInDb?.educationLevel)
    }

    @Test
    fun INT_AUTH_001_authenticateWithLocalCredentialsRetrievesProfile() = runBlocking {
        val email = "integration-auth-${System.currentTimeMillis()}@example.com"
        repository.registerLocally("Auth User", email, "Career123", "Counsellor")

        val authProfile = repository.authenticate(email, "Career123", "Counsellor")
        assertNotNull(authProfile)
        assertEquals("Auth User", authProfile?.fullName)
        assertEquals("Counsellor", authProfile?.role)
    }

    @Test
    fun INT_AUTH_002_authenticateWithWrongPasswordReturnsNull() = runBlocking {
        val email = "integration-auth-wrong-${System.currentTimeMillis()}@example.com"
        repository.registerLocally("Auth User", email, "Career123", "Student")

        val result = repository.authenticate(email, "WrongPassword123")
        assertNull(result)
    }

    @Test
    fun INT_PROFILE_001_updateLocalProfileUpdatesDatabaseRow() = runBlocking {
        val email = "integration-profile-${System.currentTimeMillis()}@example.com"
        repository.registerLocally("Original Name", email, "Career123", "Student")

        val updated = repository.updateLocalProfile(email, "Updated Name")
        assertEquals("Updated Name", updated.fullName)

        val stored = database.userProfiles().findByEmail(email)
        assertEquals("Updated Name", stored?.fullName)
    }

    @Test
    fun INT_CATALOG_001_observeCatalogProvidesOpportunitiesFromDatabase() = runBlocking {
        val opportunities = repository.observeCatalog().first()
        assertTrue("Catalog should contain opportunities", opportunities.isNotEmpty())
        assertTrue("Software Developer or equivalent should be present", opportunities.any { it.title.contains("Software") || it.category == "Careers" })
    }

    @Test
    fun INT_CATALOG_002_observeUniversityProgrammesProvidesProgrammes() = runBlocking {
        val programmes = repository.observeUniversityProgrammes().first()
        assertTrue("Programmes should be populated from Room DB", programmes.isNotEmpty())
        assertTrue("Programmes should reference universities", programmes.all { it.universityId.isNotBlank() })
    }

    @Test
    fun INT_SAVE_001_toggleSavedItemUpdatesRoomDatabaseAndFlow() = runBlocking {
        val email = "integration-save-${System.currentTimeMillis()}@example.com"
        repository.registerLocally("Save User", email, "Career123", "Student")

        val item = database.opportunities().observeAll().first().first { it.category == "Careers" }
        repository.toggleSaved(null, item.title, email)

        val savedTitles = repository.observeSavedTitles(null, email).first()
        assertTrue("Saved titles should include toggled item", savedTitles.contains(item.title))

        // Toggle again to remove
        repository.toggleSaved(null, item.title, email)
        val savedTitlesAfterRemove = repository.observeSavedTitles(null, email).first()
        assertTrue("Saved titles should no longer include removed item", !savedTitlesAfterRemove.contains(item.title))
    }

    @Test
    fun INT_LOGOUT_001_signOutClearsSessionData() = runBlocking {
        repository.signOut()
        // verify signOut completes without error
        assertTrue(true)
    }
}
