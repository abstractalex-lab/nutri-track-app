package com.fit2081.nutri_track_app.data.foodquestionnaire

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * FoodQuestionnaire entity represents the user's questionnaire response
 *
 * Stores selected food categories, meal/wake/sleep times, and health persona.
 * Linked to a Patient via foreign key on userId.
 *
 * @property userId the ID of the patient (also acts as primary key)
 * @property selectedFoods comma-separated string of selected foods
 * @property persona the selected health persona string
 * @property mealTime time of day user eats their biggest meal
 * @property sleepTime time user usually goes to sleep
 * @property wakeTime time user usually wakes up
 *
 */
@Entity(
    tableName = "food_questionnaire",
    foreignKeys = [ForeignKey(
        entity = com.fit2081.nutri_track_app.data.patient.Patient::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FoodQuestionnaire(
    @PrimaryKey val userId: String,
    val selectedFoods: String,
    val persona: String,
    val mealTime: String,
    val sleepTime: String,
    val wakeTime: String
)
