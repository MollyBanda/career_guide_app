package com.pathfinder.app.data.model

import androidx.room.*
import java.util.UUID

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val role: String,
    val educationLevel: String = "Exploring",
    val passwordSalt: String,
    val passwordHash: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "opportunities",
    indices = [Index(value = ["title", "category"], unique = true)]
)
data class OpportunityEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val subtitle: String,
    val overview: String,
    val qualifications: String = "",
    val courses: String = "",
    val skills: String = "",
    val pathways: String = "",
    val provider: String = "",
    val requirements: String = "",
    val duration: String = "",
    val deadline: String = "",
    val applicationUrl: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "university_programmes",
    foreignKeys = [
        ForeignKey(
            entity = OpportunityEntity::class,
            parentColumns = ["id"],
            childColumns = ["universityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["universityId"]), Index(value = ["universityId", "programmeName"], unique = true)]
)
data class UniversityProgrammeEntity(
    @PrimaryKey val id: String,
    val universityId: String,
    val programmeName: String,
    val award: String,
    val duration: String,
    val entryRequirements: String,
    val requiredSubjects: String,
    val facultyOrDepartment: String,
    val studyLevel: String,
    val applicationProcedure: String,
    val officialUrl: String,
    val sourceUrl: String,
    val lastVerified: String,
    val verificationNote: String = ""
)

@Entity(
    tableName = "saved_items",
    primaryKeys = ["userEmail", "opportunityId"],
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["email"],
            childColumns = ["userEmail"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OpportunityEntity::class,
            parentColumns = ["id"],
            childColumns = ["opportunityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["opportunityId"])]
)
data class SavedItemEntity(
    val userEmail: String,
    val opportunityId: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "assessment_results",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["email"],
            childColumns = ["userEmail"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userEmail"])]
)
data class AssessmentResultEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userEmail: String,
    val resultSummary: String,
    val answers: String,
    val completedAt: Long = System.currentTimeMillis(),
    val recommendations: String = ""
)
