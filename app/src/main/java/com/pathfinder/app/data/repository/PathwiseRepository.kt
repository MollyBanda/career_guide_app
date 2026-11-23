package com.pathfinder.app.data.repository

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pathfinder.app.data.local.PathwiseDatabase
import com.pathfinder.app.data.local.defaultOpportunities
import com.pathfinder.app.data.local.defaultUniversityProgrammes
import com.pathfinder.app.data.model.*
import com.pathfinder.app.data.validation.PathwiseInputValidation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val Context.pathwisePreferences by preferencesDataStore(name = "pathwise_preferences")

data class PathwiseUserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val role: String,
    val cloudSynced: Boolean = true
)

data class PathwiseAnnouncement(val id: String, val title: String, val message: String)
data class PathwiseStudentAccount(val fullName: String, val email: String)

class PathwiseRepository private constructor(private val context: Context) {
    private val database = PathwiseDatabase.get(context)
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val activeEmailKey = stringPreferencesKey("active_user_email")

    fun observeCatalog(): Flow<List<OpportunityEntity>> = database.opportunities().observeAll()
    fun observeUniversityProgrammes(): Flow<List<UniversityProgrammeEntity>> = database.universityProgrammes().observeAll()

    fun observeStudentAccounts(onError: (Exception) -> Unit = {}): Flow<List<PathwiseStudentAccount>> = callbackFlow {
        val registration = firestore.collection("users").whereEqualTo("role", "Student")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    trySend(emptyList())
                } else {
                    trySend(snapshot?.documents.orEmpty().mapNotNull { document ->
                        document.getString("email")?.let { email ->
                            val name = document.getString("fullName")?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")
                            PathwiseStudentAccount(name, email)
                        }
                    })
                }
            }
        awaitClose { registration.remove() }
    }.combine(database.userProfiles().observeProfilesByRole("Student")) { cloudAccounts, localProfiles ->
        (localProfiles.map { PathwiseStudentAccount(it.fullName, it.email) } + cloudAccounts)
            .associateBy { it.email.lowercase() }
            .values
            .sortedBy { it.fullName.lowercase() }
    }

    fun observeStudentAccountCount(onError: (Exception) -> Unit = {}): Flow<Int> =
        observeStudentAccounts(onError).map { it.size }

    fun observeLatestAnnouncement(onError: (Exception) -> Unit = {}): Flow<PathwiseAnnouncement?> = callbackFlow {
        val registration = firestore.collection("announcements")
            .orderBy("createdAt", Query.Direction.DESCENDING).limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    trySend(null)
                } else {
                    val item = snapshot?.documents?.firstOrNull()
                    trySend(item?.let { PathwiseAnnouncement(it.id, it.getString("title").orEmpty(), it.getString("message").orEmpty()) })
                }
            }
        awaitClose { registration.remove() }
    }

    suspend fun broadcastAnnouncement(uid: String, title: String, message: String) = withContext(Dispatchers.IO) {
        require(title.isNotBlank() && message.isNotBlank()) { "Enter an announcement title and message." }
        require(firebaseAuth.currentUser.isTrustedAdmin(uid)) { "Only a verified administrator can broadcast system announcements." }
        val id = java.util.UUID.randomUUID().toString()
        awaitFirebase(firestore.collection("announcements").document(id).set(mapOf(
            "id" to id,
            "title" to title.trim(),
            "message" to message.trim(),
            "createdBy" to uid,
            "createdAt" to System.currentTimeMillis()
        )))
        Unit
    }

    fun observeSavedTitles(uid: String?, localEmail: String = "", onError: (Exception) -> Unit = {}): Flow<Set<String>> = if (uid.isNullOrBlank()) {
        if (localEmail.isBlank()) kotlinx.coroutines.flow.flowOf(emptySet())
        else database.savedItems().observeTitles(localEmail).map { it.toSet() }
    } else {
        callbackFlow {
            val registration = firestore.collection("users").document(uid).collection("savedItems")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                        trySend(emptySet())
                    }
                    else trySend(snapshot?.documents.orEmpty().mapNotNull { it.getString("title") }.toSet())
                }
            awaitClose { registration.remove() }
        }
    }

    fun observeLatestAssessment(uid: String?, onError: (Exception) -> Unit = {}): Flow<AssessmentResultEntity?> = if (uid.isNullOrBlank()) {
        kotlinx.coroutines.flow.flowOf(null)
    } else {
        callbackFlow {
            val registration = firestore.collection("users").document(uid).collection("assessments")
                .orderBy("completedAt", Query.Direction.DESCENDING).limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onError(error)
                        trySend(null)
                    }
                    else {
                        val result = snapshot?.documents?.firstOrNull()?.toAssessment(uid)
                        trySend(result)
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    suspend fun initialize(): PathwiseUserProfile? = withContext(Dispatchers.IO) {
        // Insert missing deterministic catalogue IDs even when a user's database already has
        // content. Room IGNORE preserves existing/user-edited rows and makes this repeat-safe.
        val catalogue = defaultOpportunities()
        database.opportunities().insertAll(catalogue)
        // Upgrade only unchanged starter rows whose original description is still present.
        // UPDATE keeps their primary keys intact, so saved-item foreign keys are preserved.
        val unchangedStarterDescriptions = mapOf(
            "uni-malawi" to "Explore University of Malawi programmes, entry pathways, and admissions information.",
            "career-cybersecurity" to "Identifies security risks, monitors threats, and supports incident response.",
            "career-data" to "Uses statistics, programming, and domain knowledge to communicate evidence from data.",
            "career-data-engineer" to "Builds and maintains software based on user and organisation needs."
        )
        catalogue.filter { it.id in unchangedStarterDescriptions }.forEach { updated ->
            val current = database.opportunities().findById(updated.id)
            if (current != null && current.title == updated.title && current.category == updated.category && current.overview == unchangedStarterDescriptions[updated.id]) {
                database.opportunities().update(updated)
            }
        }
        // Programmes reference the stable university opportunity ID; IGNORE makes repeated seeding safe.
        val verifiedProgrammes = defaultUniversityProgrammes().filter { programme ->
            database.opportunities().findById(programme.universityId) != null
        }
        database.universityProgrammes().insertAll(verifiedProgrammes)
        val user = firebaseAuth.currentUser
        if (user == null) {
            val activeEmail = context.pathwisePreferences.data.first()[activeEmailKey] ?: return@withContext null
            val local = database.userProfiles().findByEmail(activeEmail) ?: return@withContext null
            return@withContext PathwiseUserProfile(localUid(activeEmail), local.fullName, local.email, local.role, cloudSynced = false)
        }
        val profileRef = firestore.collection("users").document(user.uid)
        var snapshot = try {
            awaitFirebase(profileRef.get())
        } catch (error: Throwable) {
            val local = user.email?.let { database.userProfiles().findByEmail(it) }
            if (local != null && error.rootCause().canUseCachedProfile()) {
                setActiveEmail(local.email)
                return@withContext PathwiseUserProfile(user.uid, local.fullName, local.email, local.role, cloudSynced = false)
            }
            throw error.rootCause()
        }
        if (!snapshot.exists()) {
            val local = user.email?.let { database.userProfiles().findByEmail(it) }
            if (local == null) {
                firebaseAuth.signOut()
                context.pathwisePreferences.edit { it.remove(activeEmailKey) }
                return@withContext null
            }
            val now = System.currentTimeMillis()
            try {
                awaitFirebase(profileRef.set(mapOf(
                    "uid" to user.uid,
                    "fullName" to local.fullName,
                    "email" to user.email,
                    "role" to local.role,
                    "createdAt" to local.updatedAt,
                    "updatedAt" to now
                )))
            } catch (error: Throwable) {
                if (error.rootCause().canUseCachedProfile()) {
                    setActiveEmail(local.email)
                    return@withContext PathwiseUserProfile(user.uid, local.fullName, local.email, local.role, cloudSynced = false)
                }
                throw error.rootCause()
            }
            snapshot = awaitFirebase(profileRef.get())
        }
        val cloudProfile = snapshot.toPathwiseProfile(user.uid)
        cloudProfile.email.let { cached ->
            runCatching {
                database.userProfiles().findByEmail(cached)?.let {
                    database.userProfiles().updateProfile(cached, cached, cloudProfile.fullName, System.currentTimeMillis())
                }
            }
        }
        migrateLegacyPrivateData(user.uid, cloudProfile.email)
        cloudProfile
    }

    suspend fun register(name: String, email: String, password: String, role: String, educationLevel: String = "Exploring"): PathwiseUserProfile? = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        require(role in setOf("Student", "Counsellor", "Admin")) { "Choose a valid account type." }
        val selectedEducationLevel = if (role == "Student") educationLevel else "Exploring"
        require(selectedEducationLevel in setOf("High School", "College / Uni", "Career Switcher", "Exploring")) { "Choose your current education level." }
        if (database.userProfiles().findByEmail(normalizedEmail) != null) return@withContext null
        try {
            awaitFirebase(firebaseAuth.createUserWithEmailAndPassword(normalizedEmail, password), timeoutSeconds = 12)
        } catch (error: Throwable) {
            val cause = error.rootCause()
            if (cause is FirebaseAuthUserCollisionException) return@withContext null
            if (cause.canUseLocalFallback()) {
                return@withContext createLocalAccount(name, normalizedEmail, password, role, selectedEducationLevel)
            }
            throw cause
        }
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val hash = passwordHash(password, salt)
        val uid = firebaseAuth.currentUser?.uid ?: error("Firebase did not return a signed-in user.")
        val now = System.currentTimeMillis()
        val localProfile = UserProfileEntity(
            email = normalizedEmail,
            fullName = name.trim(),
            role = role,
            educationLevel = selectedEducationLevel,
            passwordSalt = Base64.encodeToString(salt, Base64.NO_WRAP),
            passwordHash = Base64.encodeToString(hash, Base64.NO_WRAP),
            updatedAt = now
        )
        val cachedLocally = runCatching { database.userProfiles().insert(localProfile) }.isSuccess
        val profileData = mapOf(
            "uid" to uid,
            "fullName" to name.trim(),
            "email" to normalizedEmail,
            "role" to role,
            "educationLevel" to selectedEducationLevel,
            "emailVerified" to false,
            "createdAt" to now,
            "updatedAt" to now
        )
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            runCatching { awaitFirebase(firebaseUser.sendEmailVerification()) }
        }
        try {
            val profileRef = firestore.collection("users").document(uid)
            // set() writes the profile document to Firestore.
            awaitFirebase(profileRef.set(profileData))
        } catch (error: Throwable) {
            val cause = error.rootCause()
            if (cachedLocally && cause.canUseCachedProfile()) {
                setActiveEmail(normalizedEmail)
                return@withContext PathwiseUserProfile(uid, name.trim(), normalizedEmail, role, cloudSynced = false)
            }
            throw cause
        }
        try {
            if (!cachedLocally) database.userProfiles().insert(localProfile)
            setActiveEmail(normalizedEmail)
        } catch (error: Throwable) {
            // Firestore is authoritative; a local cache failure must not undo a confirmed cloud account/profile.
        }
        PathwiseUserProfile(uid, name.trim(), normalizedEmail, role)
    }

    /** Keeps signup usable when Firebase Auth cannot complete; never reports this profile as cloud-synced. */
    suspend fun registerLocally(name: String, email: String, password: String, role: String, educationLevel: String = "Exploring"): PathwiseUserProfile? = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        require(role in setOf("Student", "Counsellor", "Admin")) { "Choose a valid account type." }
        val selectedEducationLevel = if (role == "Student") educationLevel else "Exploring"
        require(selectedEducationLevel in setOf("High School", "College / Uni", "Career Switcher", "Exploring")) { "Choose your current education level." }
        val existing = database.userProfiles().findByEmail(normalizedEmail)
        if (existing != null && !passwordMatches(existing, password)) return@withContext null
        val profile = existing ?: return@withContext createLocalAccount(name, normalizedEmail, password, role, selectedEducationLevel)
        setActiveEmail(normalizedEmail)
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid?.takeIf { currentUser.email.equals(normalizedEmail, ignoreCase = true) }
            ?: localUid(normalizedEmail)
        PathwiseUserProfile(uid, profile.fullName, profile.email, profile.role, cloudSynced = false)
    }

    suspend fun authenticate(email: String, password: String, selectedRole: String = ""): PathwiseUserProfile? = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        val localProfile = database.userProfiles().findByEmail(normalizedEmail)
        if (firebaseAuth.currentUser?.email?.equals(normalizedEmail, ignoreCase = true) != true) {
            firebaseAuth.signOut()
            context.pathwisePreferences.edit { it.remove(activeEmailKey) }
        }
        var authenticationFailure: Throwable? = null
        try {
            awaitFirebase(firebaseAuth.signInWithEmailAndPassword(normalizedEmail, password), timeoutSeconds = 12)
        } catch (error: Throwable) {
            val cause = error.rootCause()
            authenticationFailure = cause

            // A locally-created account remains usable when Firebase is misconfigured, offline,
            // or does not yet know about it. The salted local password hash still has to match.
            if (localProfile != null && passwordMatches(localProfile, password)) {
                if (selectedRole.isNotBlank() && !localProfile.role.equals(selectedRole, ignoreCase = true)) return@withContext null
                firebaseAuth.signOut()
                setActiveEmail(normalizedEmail)
                return@withContext PathwiseUserProfile(
                    localUid(normalizedEmail),
                    localProfile.fullName,
                    normalizedEmail,
                    localProfile.role,
                    cloudSynced = false
                )
            }

            if (cause.isFirebaseInvalidCredential()) return@withContext null
            throw cause
        }
        if (firebaseAuth.currentUser == null) {
            val local = localProfile
            val failure = authenticationFailure
            if (local != null && passwordMatches(local, password) && failure?.canUseLocalFallback() == true) {
                if (selectedRole.isNotBlank() && !local.role.equals(selectedRole, ignoreCase = true)) return@withContext null
                setActiveEmail(normalizedEmail)
                return@withContext PathwiseUserProfile(localUid(normalizedEmail), local.fullName, normalizedEmail, local.role, cloudSynced = false)
            }
            throw failure ?: IllegalStateException("Firebase did not sign in this account.")
        }
        val uid = firebaseAuth.currentUser?.uid ?: error("Firebase sign-in did not provide a user ID.")
        val profileRef = firestore.collection("users").document(uid)
        var cloudProfile = try {
            awaitFirebase(profileRef.get())
        } catch (error: Throwable) {
            val cause = error.rootCause()
            if (localProfile != null && cause.canUseCachedProfile()) {
                if (selectedRole.isNotBlank() && !localProfile.role.equals(selectedRole, ignoreCase = true)) {
                    firebaseAuth.signOut()
                    return@withContext null
                }
                setActiveEmail(normalizedEmail)
                return@withContext PathwiseUserProfile(uid, localProfile.fullName, normalizedEmail, localProfile.role, cloudSynced = false)
            }
            throw cause
        }
        if (!cloudProfile.exists()) {
            // Older Firebase Auth users may predate Firestore profiles. Backfill their
            // profile under the Auth UID without replacing any existing document.
            val authUser = firebaseAuth.currentUser ?: error("Firebase sign-in did not provide a user.")
            val now = System.currentTimeMillis()
            val displayName = localProfile?.fullName?.takeIf { it.isNotBlank() }
                ?: authUser.displayName?.takeIf { it.isNotBlank() }
                ?: normalizedEmail.substringBefore("@").replace('.', ' ').replace('_', ' ')
            val role = localProfile?.role?.takeIf { it in setOf("Student", "Counsellor", "Admin") } ?: "Student"
            val createdAt = authUser.metadata?.creationTimestamp?.takeIf { it > 0 } ?: now
            val missingProfile = mapOf(
                "uid" to uid,
                "fullName" to displayName,
                "email" to normalizedEmail,
                "role" to role,
                "educationLevel" to "Exploring",
                "createdAt" to createdAt,
                "updatedAt" to now
            )
            try {
                awaitFirebase(profileRef.set(missingProfile))
            } catch (error: Throwable) {
                // A concurrent login could have created it; re-read before deciding
                // that Firestore is unavailable or the account cannot continue.
                val latest = runCatching { awaitFirebase(profileRef.get()) }.getOrNull()
                if (latest?.exists() != true) {
                    val cause = error.rootCause()
                    if (localProfile != null && cause.canUseCachedProfile()) {
                        if (selectedRole.isNotBlank() && !localProfile.role.equals(selectedRole, ignoreCase = true)) {
                            firebaseAuth.signOut()
                            return@withContext null
                        }
                        setActiveEmail(normalizedEmail)
                        return@withContext PathwiseUserProfile(uid, localProfile.fullName, normalizedEmail, localProfile.role, cloudSynced = false)
                    }
                    throw cause
                }
            }
            cloudProfile = awaitFirebase(profileRef.get())
        }
        val profile = cloudProfile.toPathwiseProfile(uid)
        val isAdminClaim = checkIsAdminToken(firebaseAuth.currentUser)
        val effectiveRole = if (isAdminClaim) "Admin" else profile.role
        migrateLegacyPrivateData(uid, normalizedEmail)
        setActiveEmail(normalizedEmail)
        PathwiseUserProfile(uid, profile.fullName, normalizedEmail, effectiveRole, cloudSynced = true)
    }

    suspend fun sendPasswordResetEmail(email: String) = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        require(normalizedEmail.contains("@")) { "Enter a valid email address." }
        awaitFirebase(firebaseAuth.sendPasswordResetEmail(normalizedEmail))
    }

    suspend fun sendVerificationEmail() = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser ?: error("No signed-in user found.")
        awaitFirebase(user.sendEmailVerification())
    }

    suspend fun checkIsAdminToken(user: FirebaseUser? = firebaseAuth.currentUser): Boolean = withContext(Dispatchers.IO) {
        if (user == null) return@withContext false
        try {
            val tokenResult = awaitFirebase(user.getIdToken(true))
            if (tokenResult.claims["admin"] == true) return@withContext true
        } catch (_: Throwable) {}
        val uidMatches = user.uid == "9FqaKqOScRco4qv4tpNqtqd08Sf1"
        val emailMatches = user.email?.equals("mphasamolly@gmail.com", ignoreCase = true) == true
        uidMatches || emailMatches
    }

    suspend fun isEmailVerified(uid: String? = null): Boolean = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser
        if (user == null) return@withContext true
        try {
            awaitFirebase(user.reload())
        } catch (_: Throwable) {}
        val verified = firebaseAuth.currentUser?.isEmailVerified == true
        if (verified) {
            runCatching {
                firestore.collection("users").document(user.uid).update("emailVerified", true)
            }
        }
        verified
    }

    fun explainAccountError(error: Throwable): String {
        val cause = error.rootCause()
        if (error.hasFirebaseConfigurationError()) {
            return "Firebase Authentication is not configured for this app yet. Password reset emails cannot be sent until Email/Password Authentication is enabled and configured for project career-app-ae1c0. Accounts created on this device while Firebase was unavailable are local only, so Firebase has no account to email yet."
        }
        return when (cause) {
            is FirebaseFirestoreException -> when (cause.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "Firebase denied profile access. Deploy firestore.rules to enable cloud sync; this device can still use its cached profile."
                FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                    "Firebase is temporarily unreachable. Check your connection and try again."
                else -> "Firestore error (${cause.code}): ${cause.message ?: "request failed"}"
            }
            is FirebaseAuthException -> when {
                cause.errorCode.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ->
                    "Email/Password sign-in is disabled in Firebase Console. Enable Email/Password under Authentication sign-in methods."
                cause.errorCode.contains("EMAIL_ALREADY_IN_USE", ignoreCase = true) -> "Email already registered. Try logging in."
                cause.errorCode.contains("WEAK_PASSWORD", ignoreCase = true) -> "Choose a password with at least 6 characters."
                cause.errorCode.contains("INVALID_EMAIL", ignoreCase = true) -> "Enter a valid email address."
                cause.errorCode.contains("INVALID_CREDENTIAL", ignoreCase = true) || cause.errorCode.contains("WRONG_PASSWORD", ignoreCase = true) ->
                    "Email or password is incorrect."
                else -> "Firebase Authentication error (${cause.errorCode}): ${cause.message ?: "request failed"}"
            }
            is FirebaseNetworkException -> "Firebase could not be reached. Check your connection and try again."
            is java.util.concurrent.TimeoutException -> "Firebase took too long to respond. A local profile is available on this device; cloud sync will resume when Firebase is configured."
            else -> cause.message ?: "The account request could not be completed."
        }
    }

    suspend fun activeProfile(): PathwiseUserProfile? = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser ?: return@withContext null
        val snapshot = awaitFirebase(firestore.collection("users").document(user.uid).get())
        if (!snapshot.exists()) return@withContext null
        snapshot.toPathwiseProfile(user.uid)
    }

    suspend fun updateProfile(uid: String, name: String, updatedEmail: String): PathwiseUserProfile = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser ?: error("Sign in before updating your profile.")
        require(user.uid == uid) { "You can only update your own profile." }
        val email = user.email ?: error("Signed-in Firebase account has no email address.")
        require(updatedEmail.trim().equals(email, ignoreCase = true)) { "Change the email in Firebase Authentication before updating the profile." }
        val now = System.currentTimeMillis()
        val profileRef = firestore.collection("users").document(uid)
        val profileSnapshot = awaitFirebase(profileRef.get())
        if (!profileSnapshot.exists()) error("Your Firestore profile document is missing.")
        val role = profileSnapshot.getString("role") ?: "Student"
        awaitFirebase(profileRef.update(mapOf("fullName" to name.trim(), "updatedAt" to now)))
        runCatching { database.userProfiles().updateProfile(email, email, name.trim(), now) }
        PathwiseUserProfile(uid, name.trim(), email, role)
    }

    suspend fun updateLocalProfile(email: String, name: String) = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        val profile = database.userProfiles().findByEmail(normalizedEmail)
            ?: error("The local account profile could not be found on this device.")
        database.userProfiles().updateProfile(normalizedEmail, normalizedEmail, name.trim(), System.currentTimeMillis())
        PathwiseUserProfile(localUid(normalizedEmail), name.trim(), normalizedEmail, profile.role, cloudSynced = false)
    }

    suspend fun updateProfileDetails(bio: String, educationSkills: String, careerPreferences: String) = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser ?: error("Sign in with Firebase to sync profile details.")
        val profileRef = firestore.collection("users").document(user.uid)
        val snapshot = awaitFirebase(profileRef.get())
        if (!snapshot.exists()) error("Your Firestore profile document is missing.")
        awaitFirebase(profileRef.update(mapOf(
            "bio" to bio.trim(),
            "educationSkills" to educationSkills.trim(),
            "careerPreferences" to careerPreferences.trim(),
            "updatedAt" to System.currentTimeMillis()
        )))
    }

    suspend fun loadProfileDetails(): Triple<String, String, String>? = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser ?: return@withContext null
        val snapshot = awaitFirebase(firestore.collection("users").document(user.uid).get())
        if (!snapshot.exists()) return@withContext null
        Triple(
            snapshot.getString("bio").orEmpty(),
            snapshot.getString("educationSkills").orEmpty(),
            snapshot.getString("careerPreferences").orEmpty()
        )
    }

    suspend fun signOut() {
        context.pathwisePreferences.edit { it.remove(activeEmailKey) }
        firebaseAuth.signOut()
    }

    suspend fun toggleSaved(uid: String?, title: String, localEmail: String = "") = withContext(Dispatchers.IO) {
        val item = database.opportunities().findByTitle(title) ?: return@withContext
        if (uid.isNullOrBlank()) {
            require(localEmail.isNotBlank() && database.userProfiles().findByEmail(localEmail) != null) {
                "Log in to save opportunities."
            }
            if (database.savedItems().contains(localEmail, item.id)) database.savedItems().delete(localEmail, item.id)
            else database.savedItems().insert(SavedItemEntity(localEmail, item.id))
            return@withContext
        }
        require(firebaseAuth.currentUser?.uid == uid) { "Sign in to manage your own saved items." }
        val savedRef = firestore.collection("users").document(uid).collection("savedItems").document(item.id)
        val current = awaitFirebase(savedRef.get())
        if (current.exists()) awaitFirebase(savedRef.delete())
        else awaitFirebase(savedRef.set(mapOf(
            "itemId" to item.id,
            "title" to item.title,
            "category" to item.category,
            "savedAt" to System.currentTimeMillis()
        )))
    }

    suspend fun saveAssessment(uid: String, summary: String, answers: List<String>) = withContext(Dispatchers.IO) {
        require(firebaseAuth.currentUser?.uid == uid) { "Sign in before saving assessment results." }
        require(PathwiseInputValidation.hasCompleteAssessment(answers)) { "Complete all assessment questions before saving." }
        val id = java.util.UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val paths = listOf("UX Design", "Data Science", "Environmental Science")
        val assessment = mapOf(
            "assessmentId" to id,
            "resultSummary" to summary,
            "answers" to answers,
            "completedAt" to now,
            "recommendations" to paths
        )
        val userRef = firestore.collection("users").document(uid)
        val batch = firestore.batch()
        batch.set(userRef.collection("assessments").document(id), assessment)
        batch.set(userRef.collection("recommendations").document(id), mapOf(
            "assessmentId" to id,
            "careerPaths" to paths,
            "createdAt" to now
        ))
        awaitFirebase(batch.commit())
        runCatching {
            database.assessments().insert(AssessmentResultEntity(
                id = id, userEmail = firebaseAuth.currentUser?.email.orEmpty(),
                resultSummary = summary, answers = answers.joinToString("\n"), completedAt = now,
                recommendations = paths.joinToString("\n")
            ))
        }
    }

    suspend fun saveOpportunityAsAdmin(uid: String, item: OpportunityEntity): Boolean = withContext(Dispatchers.IO) {
        if (!firebaseAuth.currentUser.isTrustedAdmin(uid)) return@withContext false
        database.opportunities().upsert(item.copy(updatedAt = System.currentTimeMillis()))
        true
    }

    suspend fun deleteOpportunityAsAdmin(uid: String, id: String): Boolean = withContext(Dispatchers.IO) {
        if (!firebaseAuth.currentUser.isTrustedAdmin(uid)) return@withContext false
        database.opportunities().deleteById(id)
        true
    }

    private suspend fun setActiveEmail(email: String) {
        context.pathwisePreferences.edit { it[activeEmailKey] = email }
    }

    private suspend fun createLocalAccount(
        name: String,
        email: String,
        password: String,
        role: String,
        educationLevel: String = "Exploring"
    ): PathwiseUserProfile {
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val localProfile = UserProfileEntity(
            email = email,
            fullName = name.trim(),
            role = role,
            educationLevel = educationLevel,
            passwordSalt = Base64.encodeToString(salt, Base64.NO_WRAP),
            passwordHash = Base64.encodeToString(passwordHash(password, salt), Base64.NO_WRAP)
        )
        database.userProfiles().insert(localProfile)
        setActiveEmail(email)
        return PathwiseUserProfile(localUid(email), localProfile.fullName, email, role, cloudSynced = false)
    }

    private fun localUid(email: String): String =
        "local-${java.util.UUID.nameUUIDFromBytes(email.lowercase().toByteArray()).toString()}"

    private fun passwordHash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 180_000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun passwordMatches(profile: UserProfileEntity, password: String): Boolean {
        val salt = Base64.decode(profile.passwordSalt, Base64.NO_WRAP)
        val expected = Base64.decode(profile.passwordHash, Base64.NO_WRAP)
        val actual = passwordHash(password, salt)
        return java.security.MessageDigest.isEqual(expected, actual)
    }

    private fun DocumentSnapshot.toPathwiseProfile(uid: String) = PathwiseUserProfile(
        uid = uid,
        fullName = getString("fullName").orEmpty(),
        email = getString("email") ?: firebaseAuth.currentUser?.email.orEmpty(),
        role = getString("role") ?: "Student"
    )

    private fun DocumentSnapshot.toAssessment(uid: String) = AssessmentResultEntity(
        id = getString("assessmentId") ?: id,
        userEmail = uid,
        resultSummary = getString("resultSummary").orEmpty(),
        answers = (get("answers") as? List<*>)?.filterIsInstance<String>()?.joinToString("\n").orEmpty(),
        completedAt = getLong("completedAt") ?: 0L,
        recommendations = (get("recommendations") as? List<*>)?.filterIsInstance<String>()?.joinToString("\n").orEmpty()
    )

    private suspend fun migrateLegacyPrivateData(uid: String, email: String) {
        val userRef = firestore.collection("users").document(uid)
        val localSavedTitles = database.savedItems().observeTitles(email).first()
        for (title in localSavedTitles) {
            val item = database.opportunities().findByTitle(title) ?: continue
            val ref = userRef.collection("savedItems").document(item.id)
            if (!awaitFirebase(ref.get()).exists()) {
                awaitFirebase(ref.set(mapOf(
                    "itemId" to item.id,
                    "title" to item.title,
                    "category" to item.category,
                    "savedAt" to System.currentTimeMillis()
                )))
            }
        }
        val legacyAssessment = database.assessments().observeLatest(email).first() ?: return
        val assessmentRef = userRef.collection("assessments").document(legacyAssessment.id)
        if (!awaitFirebase(assessmentRef.get()).exists()) {
            val answers = legacyAssessment.answers.split('\n').filter(String::isNotBlank)
            val paths = listOf("UX Design", "Data Science", "Environmental Science")
            val record = mapOf(
                "assessmentId" to legacyAssessment.id,
                "resultSummary" to legacyAssessment.resultSummary,
                "answers" to answers,
                "completedAt" to legacyAssessment.completedAt,
                "recommendations" to paths
            )
            val batch = firestore.batch()
            batch.set(assessmentRef, record)
            batch.set(userRef.collection("recommendations").document(legacyAssessment.id), mapOf(
                "assessmentId" to legacyAssessment.id,
                "careerPaths" to paths,
                "createdAt" to legacyAssessment.completedAt
            ))
            awaitFirebase(batch.commit())
        }
    }

    private fun <T> awaitFirebase(task: Task<T>, timeoutSeconds: Long = 30): T = try {
        Tasks.await(task, timeoutSeconds, TimeUnit.SECONDS)
    } catch (error: Throwable) {
        throw error.rootCause()
    }

    private fun Throwable.rootCause(): Throwable = generateSequence(this) { it.cause }.last()

    private fun Throwable.hasFirebaseConfigurationError(): Boolean =
        generateSequence(this) { it.cause }.any { cause ->
            cause.message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true ||
                (cause as? FirebaseAuthException)?.errorCode
                    ?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true
        }

    private fun Throwable.isFirebaseInvalidCredential(): Boolean =
        (this as? FirebaseAuthException)?.errorCode?.let {
            it.contains("INVALID_CREDENTIAL", ignoreCase = true) ||
                it.contains("WRONG_PASSWORD", ignoreCase = true) ||
                it == "invalid-credential" || it == "wrong-password"
        } == true

    private fun Throwable.canUseLocalFallback(): Boolean =
        this is FirebaseNetworkException || this is java.util.concurrent.TimeoutException ||
            hasFirebaseConfigurationError() ||
            (this as? FirebaseAuthException)?.let { authError ->
                authError.errorCode.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ||
                    authError.errorCode.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true)
            } == true

    private fun Throwable.canUseCachedProfile(): Boolean =
        this is FirebaseNetworkException || this is java.util.concurrent.TimeoutException ||
            (this as? FirebaseFirestoreException)?.code in setOf(
                FirebaseFirestoreException.Code.PERMISSION_DENIED,
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED
            )

    private fun FirebaseUser?.isTrustedAdmin(uid: String): Boolean {
        if (this == null || this.uid != uid) return false
        val tokenClaim = runCatching { Tasks.await(getIdToken(false), 10, TimeUnit.SECONDS).claims["admin"] == true }.getOrDefault(false)
        if (tokenClaim) return true
        return this.uid == "9FqaKqOScRco4qv4tpNqtqd08Sf1" || this.email?.equals("mphasamolly@gmail.com", ignoreCase = true) == true
    }

    companion object {
        @Volatile private var instance: PathwiseRepository? = null

        fun get(context: Context): PathwiseRepository = instance ?: synchronized(this) {
            instance ?: PathwiseRepository(context.applicationContext).also { instance = it }
        }
    }
}
