package com.fit2081.alex_34662901_assignment3.data.nutricoach

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * DAO interface for managing NutriCoach AI tips
 *
 */
@Dao
interface NutriCoachTipDao {

    /**
     * Insert a new tip into the database
     *
     * @param tip tip to save
     *
     */
    @Insert
    suspend fun insertTip(tip: NutriCoachTip)

    /**
     * Get all tips for a specific user in descending time order
     *
     * @param userId patient user ID
     * @return list of past tips
     *
     */
    @Query("SELECT * FROM nutri_coach_tips WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getTipsForUser(userId: String): List<NutriCoachTip>
}