package com.fit2081.alex_34662901_assignment3.data.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.alex_34662901_assignment3.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel class for interacting with patient data
 *
 * Provides logic to:
 * - Load user IDs
 * - Claim accounts
 * - Change passwords
 * - Fetch patient details
 *
 */
class PatientViewModel(private val db: AppDatabase) : ViewModel() {

    //holds list of IDs for login dropdown
    private val _userIds = MutableStateFlow<List<String>>(emptyList())
    val userIds: StateFlow<List<String>> = _userIds

    /**
     * Load all user IDs from the DB and sort them numerically
     *
     */
    fun loadUserIds() {
        viewModelScope.launch {
            _userIds.value = db.patientDao().getAllUserIds().sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE } // Numeric sort
        }
    }

    /**
     * Claim a user's account by assigning name, phone, and password
     *
     * @param userId ID to claim
     * @param name optional full name
     * @param phone phone number
     * @param password desired password
     *
     */
    fun claimAccount(userId: String, name: String?, phone: String, password: String) {
        viewModelScope.launch {
            db.patientDao().claimUser(userId, name, phone, password)
        }
    }

    /**
     * Update password for the given user
     *
     * @param userId ID to update
     * @param password new password
     *
     */
    fun setPassword(userId: String, password: String) {
        viewModelScope.launch {
            db.patientDao().setPasswordForUser(userId, password)
        }
    }

    /**
     * Retrieve patient record by userId
     *
     */
    suspend fun getPatientById(userId: String): Patient? {
        return db.patientDao().getPatientById(userId)
    }
}
