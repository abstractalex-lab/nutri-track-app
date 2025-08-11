package com.fit2081.nutri_track_app.data.foodquestionnaire

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO interface for accessing FoodQuestionnaire table in Room DB
 *
 */
@Dao
interface FoodQuestionnaireDao {

    /**
     * Inserts or updates a user's food questionnaire response
     * Replaces entry if userId already exists
     *
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(foodQuestionnaire: FoodQuestionnaire)

    /**
     * Retrieves a user's questionnaire by userId
     *
     * @param userId ID of the user
     * @return FoodQuestionnaire entry or null
     *
     */
    @Query("SELECT * FROM food_questionnaire WHERE userId = :userId")
    suspend fun getByUserId(userId: String): FoodQuestionnaire?

    /**
     * Checks if a questionnaire has been filled by the given user
     *
     * @param userId user ID to check
     * @return 1 if exists, 0 if not
     *
     */
    @Query("SELECT COUNT(*) FROM food_questionnaire WHERE userId = :userId")
    suspend fun hasFilledQuestionnaire(userId: String): Int

}
