package com.pathfinder.app.data.repository

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pathfinder.app.data.local.PathwiseDatabase
import com.pathfinder.app.data.local.defaultOpportunities
import com.pathfinder.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val Context.pathwisePreferences by preferencesDataStore(name = "pathwise_preferences")

class PathwiseRepository private constructor(private val context: Context) {
    private val database = PathwiseDatabase.get(context)
    private val activeEmailKey = stringPreferencesKey("active_user_email")

    fun observeCatalog(): Flow<List<OpportunityEntity>> = database.opportunities().observeAll()

    fun observeSavedTitles(email: String?): Flow<Set<String>> = if (email.isNullOrBlank()) {
        kotlinx.coroutines.flow.flowOf(emptySet())
    } else {
        database.savedItems().observeTitles(email).map { it.toSet() }
    }

    fun observeLatestAssessment(email: String?): Flow<AssessmentResultEntity?> = if (email.isNullOrBlank()) {
        kotlinx.coroutines.flow.flowOf(null)
    } else {
        database.assessments().observeLatest(email)
    }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (database.opportunities().count() == 0) {
            database.opportunities().insertAll(defaultOpportunities())
        }
        val email = context.pathwisePreferences.data.first()[activeEmailKey]
        email?.let { database.userProfiles().findByEmail(it) }
    }

    suspend fun register(name: String, email: String, password: String): UserProfileEntity? = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        if (database.userProfiles().findByEmail(normalizedEmail) != null) return@withContext null
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val hash = passwordHash(password, salt)
        val profile = UserProfileEntity(
            email = normalizedEmail,
            fullName = name.trim(),
            role = "Student",
            passwordSalt = Base64.encodeToString(salt, Base64.NO_WRAP),
            passwordHash = Base64.encodeToString(hash, Base64.NO_WRAP)
        )
        database.userProfiles().insert(profile)
        setActiveEmail(normalizedEmail)
        profile
    }

    suspend fun authenticate(email: String, password: String, selectedRole: String): UserProfileEntity? = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        val profile = database.userProfiles().findByEmail(normalizedEmail) ?: return@withContext null
        if (!profile.role.equals(selectedRole, ignoreCase = true)) return@withContext null
        val salt = Base64.decode(profile.passwordSalt, Base64.NO_WRAP)
        val expected = Base64.decode(profile.passwordHash, Base64.NO_WRAP)
        val actual = passwordHash(password, salt)
        if (!java.security.MessageDigest.isEqual(expected, actual)) return@withContext null
        setActiveEmail(normalizedEmail)
        profile
    }

    suspend fun activeProfile(): UserProfileEntity? = withContext(Dispatchers.IO) {
        val email = context.pathwisePreferences.data.first()[activeEmailKey] ?: return@withContext null
        database.userProfiles().findByEmail(email)
    }

    suspend fun updateProfile(email: String, name: String, updatedEmail: String): UserProfileEntity? = withContext(Dispatchers.IO) {
        val profile = database.userProfiles().findByEmail(email) ?: return@withContext null
        val newEmail = updatedEmail.trim().lowercase()
        val conflict = database.userProfiles().findByEmail(newEmail)
        if (conflict != null && newEmail != email) return@withContext null
        val updated = profile.copy(email = newEmail, fullName = name.trim(), updatedAt = System.currentTimeMillis())
        database.userProfiles().updateProfile(email, newEmail, name.trim(), System.currentTimeMillis())
        if (newEmail != email) setActiveEmail(newEmail)
        updated
    }

    suspend fun signOut() {
        context.pathwisePreferences.edit { it.remove(activeEmailKey) }
    }

    suspend fun toggleSaved(email: String, title: String) = withContext(Dispatchers.IO) {
        val item = database.opportunities().findByTitle(title) ?: return@withContext
        val saved = database.savedItems()
        if (saved.contains(email, item.id)) saved.delete(email, item.id)
        else saved.insert(SavedItemEntity(userEmail = email, opportunityId = item.id))
    }

    suspend fun saveAssessment(email: String, summary: String, answers: List<String>) = withContext(Dispatchers.IO) {
        database.assessments().insert(
            AssessmentResultEntity(
                userEmail = email,
                resultSummary = summary,
                answers = answers.joinToString("\n")
            )
        )
    }

    suspend fun saveOpportunityAsAdmin(email: String, item: OpportunityEntity): Boolean = withContext(Dispatchers.IO) {
        if (database.userProfiles().findByEmail(email)?.role != "Admin") return@withContext false
        database.opportunities().upsert(item.copy(updatedAt = System.currentTimeMillis()))
        true
    }

    suspend fun deleteOpportunityAsAdmin(email: String, id: String): Boolean = withContext(Dispatchers.IO) {
        if (database.userProfiles().findByEmail(email)?.role != "Admin") return@withContext false
        database.opportunities().deleteById(id)
        true
    }

    private suspend fun setActiveEmail(email: String) {
        context.pathwisePreferences.edit { it[activeEmailKey] = email }
    }

    private fun passwordHash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 180_000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    companion object {
        @Volatile private var instance: PathwiseRepository? = null

        fun get(context: Context): PathwiseRepository = instance ?: synchronized(this) {
            instance ?: PathwiseRepository(context.applicationContext).also { instance = it }
        }
    }
}
