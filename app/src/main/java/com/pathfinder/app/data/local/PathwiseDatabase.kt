package com.pathfinder.app.data.local

import android.content.Context
import androidx.room.*
import com.pathfinder.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET email = :newEmail, fullName = :fullName, updatedAt = :updatedAt WHERE email = :oldEmail")
    suspend fun updateProfile(oldEmail: String, newEmail: String, fullName: String, updatedAt: Long)
}

@Dao
interface OpportunityDao {
    @Query("SELECT * FROM opportunities ORDER BY category, title")
    fun observeAll(): Flow<List<OpportunityEntity>>

    @Query("SELECT * FROM opportunities WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): OpportunityEntity?

    @Query("SELECT * FROM opportunities WHERE title = :title LIMIT 1")
    suspend fun findByTitle(title: String): OpportunityEntity?

    @Query("SELECT COUNT(*) FROM opportunities")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<OpportunityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: OpportunityEntity)

    @Query("DELETE FROM opportunities WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface SavedItemDao {
    @Query("SELECT opportunities.title FROM saved_items INNER JOIN opportunities ON opportunities.id = saved_items.opportunityId WHERE saved_items.userEmail = :email ORDER BY saved_items.savedAt DESC")
    fun observeTitles(email: String): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_items WHERE userEmail = :email AND opportunityId = :itemId)")
    suspend fun contains(email: String, itemId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: SavedItemEntity)

    @Query("DELETE FROM saved_items WHERE userEmail = :email AND opportunityId = :itemId")
    suspend fun delete(email: String, itemId: String)
}

@Dao
interface AssessmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: AssessmentResultEntity)

    @Query("SELECT * FROM assessment_results WHERE userEmail = :email ORDER BY completedAt DESC LIMIT 1")
    fun observeLatest(email: String): Flow<AssessmentResultEntity?>
}

@Database(
    entities = [UserProfileEntity::class, OpportunityEntity::class, SavedItemEntity::class, AssessmentResultEntity::class],
    version = 1,
    exportSchema = true
)
abstract class PathwiseDatabase : RoomDatabase() {
    abstract fun userProfiles(): UserProfileDao
    abstract fun opportunities(): OpportunityDao
    abstract fun savedItems(): SavedItemDao
    abstract fun assessments(): AssessmentDao

    companion object {
        @Volatile private var instance: PathwiseDatabase? = null

        fun get(context: Context): PathwiseDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PathwiseDatabase::class.java,
                "pathwise.db"
            ).build().also { instance = it }
        }
    }
}
