package com.alexbui.nutritrack.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexbui.nutritrack.data.AppDatabase
import com.alexbui.nutritrack.data.foodquestionnaire.FoodQuestionnaire
import com.alexbui.nutritrack.data.nutricoach.NutriCoachTip
import com.alexbui.nutritrack.data.patient.Patient
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * GenAIViewModel manages prompts to the Generative AI model (Gemini) and exposes state to the UI
 *
 * Handles two prompt types:
 * - Patient view: personalized fruit improvement message
 * - Clinician view: insight pattern generation across multiple users
 *
 * Model name is fetched dynamically via Firebase Remote Config to avoid
 * hardcoding and allow updates without redeployment
 */
class GenAIViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UIState>(UIState.Initial)
    val uiState: StateFlow<UIState> = _uiState

    // Remote Config instance with 1 hour cache expiry
    private val remoteConfig = FirebaseRemoteConfig.getInstance().also { config ->
        config.setConfigSettingsAsync(
            remoteConfigSettings { minimumFetchIntervalInSeconds = 3600 }
        )
        // Fallback default if Remote Config fetch fails
        config.setDefaultsAsync(mapOf("gemini_model_name" to "gemini-2.5-flash-lite"))
    }

    /**
     * Fetches the model name from Remote Config and returns a GenerativeModel instance
     * Falls back to default if fetch fails
     */
    private suspend fun getGenerativeModel() = try {
        remoteConfig.fetchAndActivate().await()
        val modelName = remoteConfig.getString("gemini_model_name")
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName)
    } catch (_: Exception) {

        // Fallback to hardcoded default if Remote Config fails
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash-lite")
    }

    /**
     * sendPromptPatient generates a personalized fruit tip using a single patient's data
     *
     * @param patient Patient object from Room
     * @param foodAnswers associated FoodQuestionnaire entry, if any
     * @param db Room database instance (used to persist tip)
     */
    fun sendPromptPatient(patient: Patient, foodAnswers: FoodQuestionnaire?, db: AppDatabase, searchedFruit: String? = null) {
        _uiState.value = UIState.Loading

        // buildString constructs a prompt string using patient scores + questionnaire data
        val userId = patient.userId
        val prompt = buildString {
            append("Generate a short encouraging message to help someone improve their fruit intake.\n\n")
            append("The user is a ${patient.sex}.\n")
            append("Their total food quality score is ${patient.heifaTotalScore}, which it represents Healthy Eating Index for Australian adults (HEIFA) score.\n")
            append("Relevant component scores:\n")
            append("- Discretionary: ${patient.discretionaryScore}\n")
            append("- Vegetables: ${patient.vegetablesScore}\n")
            append("- Fruits: ${patient.fruitsScore}\n")
            append("- Grains and cereals: ${patient.grainsCerealsScore + patient.wholeGrainsScore}\n")
            append("- Meat and alternatives: ${patient.meatAlternativesScore}\n")
            append("- Dairy and alternatives: ${patient.dairyAlternativesScore}\n")
            append("- Water: ${patient.waterScore}\n")
            append("- Fat: ${patient.saturatedFatScore + patient.unsaturatedFatScore}\n")
            append("- Water: ${patient.sodiumScore}\n")
            append("- Sugar: ${patient.sugarScore}\n")
            append("- Water: ${patient.alcoholScore}\n")

            // Nullable append questionnaire responses if available
            foodAnswers?.let {
                append("\nQuestionnaire data:\n")
                append("- Preferred foods: ${it.selectedFoods}\n")
                append("- Selected persona: ${it.persona}\n")
                append("- Wake-up time: ${it.wakeTime}\n")
                append("- Sleep time: ${it.sleepTime}\n")
                append("- Main meal time: ${it.mealTime}\n")
            }

            searchedFruit?.let {
                append("\nThe user recently searched for: $it\n")
                append("If appropriate, incorporate this fruit into your suggestion.\n")
            }
            append("\nUse the data above to make the message relevant and motivating. Make it about 300-350 characters, and can make it colorful by adding some emojis aside.")
        }

        // Send prompt to Gemini API
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val model = getGenerativeModel()
                val response = model.generateContent(prompt)
                val output = response.text ?: "No output"
                _uiState.value = UIState.Success(output)
                val tip = NutriCoachTip(userId = userId, tipText = output)
                db.nutriCoachTipDao().insertTip(tip)
            } catch (e: Exception) {
                _uiState.value = UIState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * sendPromptClinical generates 3 clinical insight patterns using all patient data
     *
     * @param context required to get database instance
     */
    fun sendPromptClinical(context: Context) {
        _uiState.value = UIState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {

                // Get database instance, and fetch all patients from Room DB
                val db = AppDatabase.getDatabase(context)
                val patients = db.patientDao().getAllPatients()

                // buildString constructs a clinical prompt string using multiple patients data
                val prompt = buildString {
                    append("You are analyzing nutritional data from a dataset.\n")
                    append("Each user has multiple scores (e.g., fruits, vegetables, saturated fat, sugar, etc.) and a total HEIFA score.\n")
                    append("Below is the anonymized dataset:\n\n")

                    patients.forEachIndexed { index, p ->
                        append("User ${index + 1}: ")
                        append("Sex=${p.sex}, ")
                        append("Fruits=${p.fruitsScore}, Vegetables=${p.vegetablesScore}, ")
                        append("Grains=${p.grainsCerealsScore}, WholeGrains=${p.wholeGrainsScore}, ")
                        append("Sugar=${p.sugarScore}, SaturatedFat=${p.saturatedFatScore}, ")
                        append("TotalHEIFA=${p.heifaTotalScore}\n")
                    }

                    append("\nNow, generate 3 unique and insightful patterns from this data. Summarize each in 3-4 sentences.")
                    append("\nSkip any introduction or greetings.")
                }

                // Send prompt to Gemini API, and update UI state with response
                val model = getGenerativeModel()
                val response = model.generateContent(prompt)
                val output = response.text ?: "No output"
                _uiState.value = UIState.Success(output)

            } catch (e: Exception) {
                _uiState.value = UIState.Error(e.message ?: "Unknown error")
            }
        }
    }
}