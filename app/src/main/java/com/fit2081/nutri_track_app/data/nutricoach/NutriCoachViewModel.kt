package com.fit2081.nutri_track_app.data.nutricoach

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ViewModel for fetching fruit information via FruityVice API
 *
 * Exposes two state variables:
 * - fruitInfo: holds successful response
 * - fruitError: holds error string if failed
 *
 */
class NutriCoachViewModel : ViewModel() {
    var fruitInfo = mutableStateOf<FruityViceResponse?>(null)
    var fruitError = mutableStateOf<String?>(null)

    //retrofit service instance for FruityVice API
    private val fruityService = Retrofit.Builder()
        .baseUrl("https://www.fruityvice.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FruityViceApiService::class.java)

    /**
     * Fetch nutritional info for a given fruit name
     *
     * Updates fruitInfo or fruitError accordingly
     *
     * @param fruitName name of fruit to look up
     *
     */
    fun fetchFruitInfo(fruitName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = fruityService.getFruit(fruitName.lowercase())
                fruitInfo.value = response
                fruitError.value = null
            } catch (_: Exception) {
                fruitInfo.value = null
                fruitError.value = "Could not find info for \"$fruitName\""
            }
        }
    }
}
