package com.fit2081.alex_34662901_assignment3.data.nutricoach

/**
 * Response data class for FruityVice API
 *
 * Represents nutritional and classification info for a fruit
 *
 * @property name name of the fruit
 * @property family botanical family
 * @property genus botanical genus
 * @property order botanical order
 * @property nutritions nested object containing nutrition facts
 *
 */
data class FruityViceResponse(
    val name: String,
    val family: String,
    val genus: String,
    val order: String,
    val nutritions: Nutrition
)


/**
 * Nutrition values returned by FruityVice API
 *
 * @property carbohydrates grams of carbs
 * @property protein grams of protein
 * @property fat grams of fat
 * @property calories energy content
 * @property sugar grams of sugar
 *
 */
data class Nutrition(
    val carbohydrates: Float,
    val protein: Float,
    val fat: Float,
    val calories: Int,
    val sugar: Float
)
