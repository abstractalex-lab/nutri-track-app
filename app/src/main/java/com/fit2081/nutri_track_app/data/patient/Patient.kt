package com.fit2081.nutri_track_app.data.patient

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing a patient in the NutriTrack system
 *
 * Each patient has:
 * - A unique userId
 * - Optional name, phone number, and password (set when claimed)
 * - Sex (used to calculate which HEIFA score applies)
 * - Multiple component scores contributing to the total HEIFA score
 *
 */
@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey val userId: String,
    var name: String? = null,
    var phoneNumber: String? = null,
    var password: String? = null,
    val sex: String,
    val heifaTotalScore: Float,
    val discretionaryScore: Float,
    val vegetablesScore: Float,
    val fruitsScore: Float,
    val grainsCerealsScore: Float,
    val wholeGrainsScore: Float,
    val meatAlternativesScore: Float,
    val sodiumScore: Float,
    val alcoholScore: Float,
    val dairyAlternativesScore: Float,
    val waterScore: Float,
    val sugarScore: Float,
    val saturatedFatScore: Float,
    val unsaturatedFatScore: Float
)
