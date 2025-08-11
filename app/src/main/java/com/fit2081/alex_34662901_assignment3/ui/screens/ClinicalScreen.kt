package com.fit2081.alex_34662901_assignment3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.fit2081.alex_34662901_assignment3.data.AppDatabase

/**
 * ClinicianLoginScreen composable handles secure access for clinicians
 *
 * Allows clinicians to:
 * - Enter a predefined access key
 * - Navigate to the Clinician Dashboard if correct
 *
 * Displays an error if the access key is incorrect
 *
 * @param navController handles navigation flow
 */
@Composable
fun ClinicianLoginScreen(navController: NavHostController) {
    var inputAccessKey by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Clinician Login",
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        //text field in password-style for access key input
        OutlinedTextField(
            value = inputAccessKey,
            onValueChange = { inputAccessKey = it },
            isError = errorMessage != null,
            label = { Text("Enter Clinician Access Key") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        //display error text if access key fails
        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        //login button that checks key and routes accordingly
        Button(
            onClick = {
                if (inputAccessKey == "dollar-entry-apples") {
                    navController.navigate("clinician_dashboard") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                } else {
                    errorMessage = "Invalid clinician key. Please try again"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Login")
        }
    }
}


/**
 * ClinicianDashboardScreen shows analytical view for clinicians
 *
 * Provides:
 * - Average HEIFA scores (male & female)
 * - AI-generated insights using GenAIViewModel on overviews of the patients data
 * - Navigation back to Settings
 *
 * @param navController controls route changes
 */
@Composable
fun ClinicianDashboardScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    //average score holders
    var maleAvg by remember { mutableStateOf<Float?>(null) }
    var femaleAvg by remember { mutableStateOf<Float?>(null) }

    //genAI integration
    val genAIViewModel = remember { GenAIViewModel() }
    val uiState by genAIViewModel.uiState.collectAsState()

    //fetch and compute average HEIFA scores on launch
    LaunchedEffect(Unit) {
        val patients = db.patientDao().getAllPatients()
        val maleScores = patients.filter { it.sex.equals("Male", ignoreCase = true) }.map { it.heifaTotalScore }
        val femaleScores = patients.filter { it.sex.equals("Female", ignoreCase = true) }.map { it.heifaTotalScore }

        maleAvg = if (maleScores.isNotEmpty()) maleScores.average().toFloat() else null
        femaleAvg = if (femaleScores.isNotEmpty()) femaleScores.average().toFloat() else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Clinician Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Average HEIFA (Male)  :  ${maleAvg?.let { "%.2f".format(it) } ?: "Loading..."}")
        Text("Average HEIFA (Female):  ${femaleAvg?.let { "%.2f".format(it) } ?: "Loading..."}")

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            //trigger GenAI insight generation
            Button(
                onClick = { genAIViewModel.sendPromptClinical(context) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Find Data Pattern")
            }

            //return to Settings screen
            Button(
                onClick = {
                    navController.navigate("settings") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //handle UIState feedback from GenAI
        when (uiState) {
            is UIState.Loading -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Analyzing data...")
                }
            }
            is UIState.Success -> {
                val tips = (uiState as UIState.Success).result.split("\n\n")
                tips.forEach { tip ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Text(
                            text = tip.trim(),
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            is UIState.Error -> {
                Text("Error: ${(uiState as UIState.Error).message}", color = Color.Red)
            }
            else -> {}
        }
    }
}

