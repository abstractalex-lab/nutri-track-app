package com.alexbui.nutritrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alexbui.nutritrack.data.foodquestionnaire.FoodQuestionnaire
import com.alexbui.nutritrack.data.foodquestionnaire.FoodQuestionnaireDao
import com.alexbui.nutritrack.data.nutricoach.NutriCoachTip
import com.alexbui.nutritrack.data.nutricoach.NutriCoachTipDao
import com.alexbui.nutritrack.data.patient.Patient
import com.alexbui.nutritrack.data.patient.PatientDao
import com.alexbui.nutritrack.data.seed.SeedFlag
import com.alexbui.nutritrack.data.seed.SeedFlagDao

/**
 * Singleton Room database for the NutriTrack app
 *
 * Includes tables:
 * - patients
 * - food_questionnaire
 * - nutri_coach_tips
 * - seed_flags
 *
 * Provides DAO accessors for each table
 *
 */
@Database(
    entities = [Patient::class, FoodQuestionnaire::class, NutriCoachTip::class, SeedFlag::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun foodQuestionnaireDao(): FoodQuestionnaireDao
    abstract fun nutriCoachTipDao(): NutriCoachTipDao
    abstract fun seedFlagDao(): SeedFlagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns a singleton instance of the Room database
         *
         * @param context app context
         *
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutritrack_db_A3"
                )
                    .fallbackToDestructiveMigration(false)
                    .enableMultiInstanceInvalidation()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
