package com.fit2081.nutri_track_app.data.nutricoach

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit service interface for FruityVice API
 *
 */
interface FruityViceApiService {

    /**
     * Sends GET request to FruityVice API to fetch fruit info
     *
     * @param fruitName name of the fruit in path param
     * @return FruityViceResponse object
     *
     */
    @GET("api/fruit/{fruitName}")
    suspend fun getFruit(@Path("fruitName") fruitName: String): FruityViceResponse
}