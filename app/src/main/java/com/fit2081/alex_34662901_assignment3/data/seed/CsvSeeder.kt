package com.fit2081.alex_34662901_assignment3.data.seed

import android.content.Context
import com.fit2081.alex_34662901_assignment3.data.AppDatabase
import com.fit2081.alex_34662901_assignment3.data.patient.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * CsvSeeder handles one-time CSV-based population of patient data
 *
 * - Reads from assignment_1.csv in assets/
 * - Parses and maps rows to Patient entities
 * - Skips if already seeded (checked via SeedFlag)
 *
 * @param context Android app context for file access
 * @param db reference to AppDatabase
 *
 */
class CsvSeeder(private val context: Context, private val db: AppDatabase) {

    /**
     * Performs seeding only once, checking via seed_flag table
     */
    suspend fun seedIfNeeded() {
        withContext(Dispatchers.IO) {
            val patientDao = db.patientDao()
            val seedFlagDao = db.seedFlagDao()

            //check if CSV has already been seeded
            if (seedFlagDao.getFlag("csv")?.seeded == true)

                //already seeded
                return@withContext

            //read from assets
            val patients = mutableListOf<Patient>()
            context.assets.open("assignment_1.csv").bufferedReader().useLines { lines ->
                val iterator = lines.iterator()
                if (!iterator.hasNext()) return@useLines

                val header = iterator.next().split(",").map { it.trim() }

                fun col(name: String): Int {
                    val index = header.indexOfFirst { it.equals(name, ignoreCase = true) }
                    if (index == -1) throw IllegalArgumentException("Missing column: $name\nAvailable: $header")
                    return index
                }

                while (iterator.hasNext()) {
                    val values = iterator.next().split(",")
                    if (values.size < header.size) continue

                    val sex = values[col("Sex")].trim()
                    val get = { male: String, female: String ->
                        values.getOrNull(col(if (sex.equals("Male", true)) male else female))
                            ?.toFloatOrNull() ?: 0f
                    }

                    val patient = Patient(
                        userId = values[col("User_ID")].trim(),
                        phoneNumber = values[col("PhoneNumber")].trim(),
                        name = null,
                        password = null,
                        sex = sex,
                        heifaTotalScore = get("HEIFAtotalscoreMale", "HEIFAtotalscoreFemale"),
                        discretionaryScore = get(
                            "DiscretionaryHEIFAscoreMale",
                            "DiscretionaryHEIFAscoreFemale"
                        ),
                        vegetablesScore = get(
                            "VegetablesHEIFAscoreMale",
                            "VegetablesHEIFAscoreFemale"
                        ),
                        fruitsScore = get("FruitHEIFAscoreMale", "FruitHEIFAscoreFemale"),
                        grainsCerealsScore = get(
                            "GrainsandcerealsHEIFAscoreMale",
                            "GrainsandcerealsHEIFAscoreFemale"
                        ),
                        wholeGrainsScore = get(
                            "WholegrainsHEIFAscoreMale",
                            "WholegrainsHEIFAscoreFemale"
                        ),
                        meatAlternativesScore = get(
                            "MeatandalternativesHEIFAscoreMale",
                            "MeatandalternativesHEIFAscoreFemale"
                        ),
                        dairyAlternativesScore = get(
                            "DairyandalternativesHEIFAscoreMale",
                            "DairyandalternativesHEIFAscoreFemale"
                        ),
                        sodiumScore = get("SodiumHEIFAscoreMale", "SodiumHEIFAscoreFemale"),
                        alcoholScore = get("AlcoholHEIFAscoreMale", "AlcoholHEIFAscoreFemale"),
                        waterScore = get("WaterHEIFAscoreMale", "WaterHEIFAscoreFemale"),
                        sugarScore = get("SugarHEIFAscoreMale", "SugarHEIFAscoreFemale"),
                        saturatedFatScore = get(
                            "SaturatedFatHEIFAscoreMale",
                            "SaturatedFatHEIFAscoreFemale"
                        ),
                        unsaturatedFatScore = get(
                            "UnsaturatedFatHEIFAscoreMale",
                            "UnsaturatedFatHEIFAscoreFemale"
                        )
                    )

                    patients.add(patient)
                }
            }

            //insert into Room
            patientDao.insertAll(patients)

            //mark CSV as seeded
            seedFlagDao.insertFlag(SeedFlag("csv", true))
        }
    }
}
