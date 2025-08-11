package com.fit2081.nutri_track_app.data.patient

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO interface for accessing and modifying patient records
 *
 */
@Dao
interface PatientDao {

    /**
     * Fetch all user IDs from the patients table
     *
     */
    @Query("SELECT userId FROM patients")
    suspend fun getAllUserIds(): List<String>

    /**
     * Update only the password for a specific user
     *
     */
    @Query("UPDATE patients SET password = :password WHERE userId = :userId")
    suspend fun setPasswordForUser(userId: String, password: String)

    /**
     * Set name, phone number, and password when user claims their account
     *
     */
    @Query("UPDATE patients SET name = :name, phoneNumber = :phone, password = :password WHERE userId = :userId")
    suspend fun claimUser(userId: String, name: String?, phone: String, password: String)

    /**
     * Insert or update a single patient record
     *
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient)

    /**
     * Insert or update a list of patient records
     *
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<Patient>)

    /**
     * Get a specific patient by ID
     *
     */
    @Query("SELECT * FROM patients WHERE userId = :userId")
    suspend fun getPatientById(userId: String): Patient?

    /**
     * Get all patients from the table
     *
     */
    @Query("SELECT * FROM patients")
    suspend fun getAllPatients(): List<Patient>
}
