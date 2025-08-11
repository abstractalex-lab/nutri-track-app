package com.fit2081.nutri_track_app.data.foodquestionnaire

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fit2081.nutri_track_app.data.AppDatabase

/**
 * Factory class to create FoodQuestionnaireViewModel with Room DB dependency
 *
 * Required to provide context when injecting into composables
 *
 * @param context application context to retrieve the singleton DB
 *
 */
@Suppress("UNCHECKED_CAST")
class FoodQuestionnaireViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getDatabase(context.applicationContext)
        return FoodQuestionnaireViewModel(db) as T
    }
}
