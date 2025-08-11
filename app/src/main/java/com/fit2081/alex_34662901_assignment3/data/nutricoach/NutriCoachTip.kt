package com.fit2081.alex_34662901_assignment3.data.nutricoach

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class for storing AI-generated coaching tips
 *
 * Saved per user and sorted by timestamp
 *
 * @property id auto-generated primary key
 * @property userId the patient this tip belongs to
 * @property tipText the full tip text (from AI)
 * @property timestamp epoch time saved for sorting
 *
 */
@Entity(tableName = "nutri_coach_tips")
data class NutriCoachTip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val tipText: String,
    val timestamp: Long = System.currentTimeMillis()
)