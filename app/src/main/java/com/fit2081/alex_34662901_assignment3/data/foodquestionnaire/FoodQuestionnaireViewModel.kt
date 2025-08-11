package com.fit2081.alex_34662901_assignment3.data.foodquestionnaire

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.alex_34662901_assignment3.data.AppDatabase
import kotlinx.coroutines.launch

/**
 * ViewModel for managing food questionnaire logic
 *
 * Provides methods to:
 * - Check if a user has filled the form
 * - Retrieve existing answers
 * - Save or update answers
 *
 * @param db Room database instance
 *
 */
class FoodQuestionnaireViewModel(private val db: AppDatabase) : ViewModel() {

    /**
     * Returns true if the user has filled their questionnaire
     *
     * @param userId the user ID
     *
     */
    suspend fun hasUserFilled(userId: String): Boolean {
        return db.foodQuestionnaireDao().hasFilledQuestionnaire(userId) > 0
    }

    /**
     * Retrieves the questionnaire for the given user
     *
     * @param userId the user ID
     *
     */
    suspend fun getByUserId(userId: String): FoodQuestionnaire? {
        return db.foodQuestionnaireDao().getByUserId(userId)
    }

    /**
     * Inserts or updates the given form into Room DB
     *
     * @param form the questionnaire form data to save
     *
     */
    fun insertOrUpdate(form: FoodQuestionnaire) {
        viewModelScope.launch {
            db.foodQuestionnaireDao().insertOrUpdate(form)
        }
    }

}
