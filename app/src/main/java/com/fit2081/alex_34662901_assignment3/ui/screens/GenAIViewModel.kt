package com.fit2081.alex_34662901_assignment3.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fit2081.alex_34662901_assignment3.BuildConfig
import com.fit2081.alex_34662901_assignment3.data.AppDatabase
import com.fit2081.alex_34662901_assignment3.data.foodquestionnaire.FoodQuestionnaire
import com.fit2081.alex_34662901_assignment3.data.nutricoach.NutriCoachTip
import com.fit2081.alex_34662901_assignment3.data.patient.Patient
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * GenAIViewModel manages prompts to the Generative AI model (Gemini) and exposes state to the UI
 *
 * Handles two prompt types:
 * - Patient view: personalized fruit improvement message
 * - Clinician view: insight pattern generation across multiple users
 */
class GenAIViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UIState>(UIState.Initial)
    val uiState: StateFlow<UIState> = _uiState

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )


    /**
     * sendPromptPatient generates a personalized fruit tip using a single patient's data
     *
     * @param patient Patient object from Room
     * @param foodAnswers associated FoodQuestionnaire entry, if any
     * @param db Room database instance (used to persist tip)
     */
    fun sendPromptPatient(patient: Patient, foodAnswers: FoodQuestionnaire?, db: AppDatabase) {
        _uiState.value = UIState.Loading

        //buildString constructs a prompt string using patient scores + questionnaire data
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

            //nullable append questionnaire responses if available
            foodAnswers?.let {
                append("\nQuestionnaire data:\n")
                append("- Preferred foods: ${it.selectedFoods}\n")
                append("- Selected persona: ${it.persona}\n")
                append("- Wake-up time: ${it.wakeTime}\n")
                append("- Sleep time: ${it.sleepTime}\n")
                append("- Main meal time: ${it.mealTime}\n")
            }

            append("\nUse the data above to make the message relevant and motivating. Make it about 300-350 characters, and can make it colorful by adding some emojis aside.")
        }

        //send prompt to Gemini API
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                val output = response.text ?: "No output"

                //update UI state with result
                _uiState.value = UIState.Success(output)

                //save result to NutriCoachTip table
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

                //get database instance, and fetch all patients from Room DB
                val db = AppDatabase.getDatabase(context)
                val patients = db.patientDao().getAllPatients()

                //buildString constructs a clinical prompt string using multiple patients data
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

                //send prompt to Gemini API, and update UI state with response
                val response = generativeModel.generateContent(prompt)
                val output = response.text ?: "No output"
                _uiState.value = UIState.Success(output)

            } catch (e: Exception) {
                _uiState.value = UIState.Error(e.message ?: "Unknown error")
            }
        }
    }
}