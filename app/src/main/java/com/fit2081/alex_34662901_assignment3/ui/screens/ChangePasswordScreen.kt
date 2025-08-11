package com.fit2081.alex_34662901_assignment3.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fit2081.alex_34662901_assignment3.data.AppDatabase
import com.fit2081.alex_34662901_assignment3.data.patient.PatientViewModel
import com.fit2081.alex_34662901_assignment3.data.patient.PatientViewModelFactory
import kotlinx.coroutines.launch

/**
 * ChangePasswordScreen composable allows a logged-in user to change their password
 *
 * Features:
 * - Verifies current password against Room DB
 * - Ensures new password meets length and match requirements
 * - Updates password in DB upon validation
 *
 * @param navController handles navigation
 * @param userId unique identifier of the currently logged-in user
 */
@Composable
fun ChangePasswordScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val patientViewModel: PatientViewModel = viewModel(factory = PatientViewModelFactory(context))
    val scope = rememberCoroutineScope()

    //form input states
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    //error flag states
    var oldPasswordError by remember { mutableStateOf(false) }
    var newPasswordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Text("Change Password", style = MaterialTheme.typography.headlineSmall, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(16.dp))

        //current password field
        OutlinedTextField(
            value = oldPassword,
            onValueChange = {
                oldPassword = it
                oldPasswordError = false
            },
            label = { Text("Current Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            isError = oldPasswordError,
            singleLine = true
        )

        //error message for incorrect old password
        if (oldPasswordError && oldPassword.isNotBlank()) {
            Text("Incorrect current password", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(12.dp))

        //new password field
        OutlinedTextField(
            value = newPassword,
            onValueChange = {
                newPassword = it
                newPasswordError = false
            },
            label = { Text("New Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            isError = newPasswordError,
            singleLine = true
        )

        //error message for new password too short
        if (newPasswordError && newPassword.length < 8 && newPassword.isNotBlank()) {
            Text("New password must be at least 8 characters", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(12.dp))

        //confirm new password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = false
            },
            label = { Text("Confirm New Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            isError = confirmPasswordError,
            singleLine = true
        )

        //error message if passwords do not match
        if (confirmPasswordError && confirmPassword.isNotBlank()) {
            Text("Password does not match", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        //confirm button to trigger password update
        Button(
            onClick = {

                //reset all error states
                oldPasswordError = false
                newPasswordError = false
                confirmPasswordError = false

                //shortcut if all fields are blank
                val allBlank = oldPassword.isBlank() && newPassword.isBlank() && confirmPassword.isBlank()
                if (allBlank) {
                    oldPasswordError = true
                    newPasswordError = true
                    return@Button
                }

                scope.launch {
                    val patient = db.patientDao().getPatientById(userId)

                    //DB check of current password
                    if (patient == null || patient.password != oldPassword) {
                        oldPasswordError = true
                        return@launch
                    }

                    //validate new password only
                    if (newPassword.isBlank() || newPassword.length < 8) {
                        newPasswordError = true
                        return@launch
                    }

                    //validate confirm password only if new password passed
                    if (confirmPassword != newPassword) {
                        confirmPasswordError = true
                        return@launch
                    }

                    //apply and update password
                    patientViewModel.setPassword(userId, newPassword)
                    Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm")
        }
    }
}
