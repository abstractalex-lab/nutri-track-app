package com.fit2081.alex_34662901_assignment3.ui.screens

/**
 * UIState class representing the different states of the GenAI UI flow
 *
 * It is used by GenAIViewModel to emit updates based on generation progress:
 * - [Initial]: idle state before any interaction
 * - [Loading]: generation is in progress
 * - [Success]: result has been generated successfully
 * - [Error]: an error occurred during generation
 */
sealed class UIState {
    object Initial : UIState()
    object Loading : UIState()
    data class Success(val result: String) : UIState()
    data class Error(val message: String) : UIState()
}