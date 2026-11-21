package com.pathfinder.app.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import com.pathfinder.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserProfileEntity?

    @Query("SELECT email FROM user_profiles WHERE role = :role")
    fun observeEmailsByRole(role: String): Flow<List<String>>

    @Query("SELECT * FROM user_profiles WHERE role = :role ORDER BY fullName COLLATE NOCASE")
    fun observeProfilesByRole(role: String): Flow<List<UserProfileEntity>>

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

    @Update
    suspend fun update(item: OpportunityEntity)

    @Query("DELETE FROM opportunities WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface UniversityProgrammeDao {
    @Query("SELECT * FROM university_programmes ORDER BY universityId, programmeName COLLATE NOCASE")
    fun observeAll(): Flow<List<UniversityProgrammeEntity>>

    @Query("SELECT * FROM university_programmes WHERE universityId = :universityId ORDER BY programmeName COLLATE NOCASE")
    fun observeForUniversity(universityId: String): Flow<List<UniversityProgrammeEntity>>

    @Query("SELECT * FROM university_programmes WHERE universityId = :universityId ORDER BY programmeName COLLATE NOCASE")
    suspend fun findForUniversity(universityId: String): List<UniversityProgrammeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<UniversityProgrammeEntity>)
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
    entities = [UserProfileEntity::class, OpportunityEntity::class, UniversityProgrammeEntity::class, SavedItemEntity::class, AssessmentResultEntity::class],
    version = 3,
    exportSchema = true
)
abstract class PathwiseDatabase : RoomDatabase() {
    abstract fun userProfiles(): UserProfileDao
    abstract fun opportunities(): OpportunityDao
    abstract fun universityProgrammes(): UniversityProgrammeDao
    abstract fun savedItems(): SavedItemDao
    abstract fun assessments(): AssessmentDao

    companion object {
        @Volatile private var instance: PathwiseDatabase? = null

        fun get(context: Context): PathwiseDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PathwiseDatabase::class.java,
                "pathwise.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS `university_programmes` (`id` TEXT NOT NULL, `universityId` TEXT NOT NULL, `programmeName` TEXT NOT NULL, `award` TEXT NOT NULL, `duration` TEXT NOT NULL, `entryRequirements` TEXT NOT NULL, `requiredSubjects` TEXT NOT NULL, `facultyOrDepartment` TEXT NOT NULL, `studyLevel` TEXT NOT NULL, `applicationProcedure` TEXT NOT NULL, `officialUrl` TEXT NOT NULL, `sourceUrl` TEXT NOT NULL, `lastVerified` TEXT NOT NULL, `verificationNote` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`universityId`) REFERENCES `opportunities`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_university_programmes_universityId` ON `university_programmes` (`universityId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_university_programmes_universityId_programmeName` ON `university_programmes` (`universityId`, `programmeName`)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `assessment_results` ADD COLUMN `recommendations` TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
