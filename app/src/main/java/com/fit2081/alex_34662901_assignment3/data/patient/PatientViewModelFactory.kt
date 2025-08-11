package com.fit2081.alex_34662901_assignment3.data.patient

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fit2081.alex_34662901_assignment3.data.AppDatabase

/**
 * Factory class for creating PatientViewModel with Room DB access
 *
 * Required for injection into composables using ViewModelProvider
 *
 * @param context application context used to retrieve DB instance
 *
 */
@Suppress("UNCHECKED_CAST")
class PatientViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getDatabase(context)
        return PatientViewModel(db) as T
    }
}
