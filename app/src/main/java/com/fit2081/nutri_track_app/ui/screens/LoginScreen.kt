package com.fit2081.nutri_track_app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fit2081.nutri_track_app.data.foodquestionnaire.FoodQuestionnaireViewModel
import com.fit2081.nutri_track_app.data.patient.PatientViewModel
import com.fit2081.nutri_track_app.data.patient.PatientViewModelFactory
import com.fit2081.nutri_track_app.data.foodquestionnaire.FoodQuestionnaireViewModelFactory
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.fit2081.nutri_track_app.data.patient.Patient
import kotlinx.coroutines.delay

/**
 * Enum class LoginMode to switch between login and registration screen behavior
 */
enum class LoginMode {
    LOGIN,
    REGISTER
}

/**
 * Composable function LoginScreen for handling login and account claiming in NutriTrack
 *
 * This screen allows users to:
 * - Select their User ID from a dropdown
 * - Login using a password
 * - Or register/claim their account with phone number and new password
 *
 * Supports two modes:
 * - LOGIN: Verifies credentials and navigates based on form status
 * - REGISTER: Allows claiming unclaimed IDs after phone validation
 *
 * Data is validated via PatientViewModel and FoodQuestionnaireViewModel
 *
 * @param navController used to navigate to Home or Questionnaire screen
 *
 */
@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val viewModel: PatientViewModel = viewModel(factory = PatientViewModelFactory(context))
    val foodQuestionnaireViewModel: FoodQuestionnaireViewModel = viewModel(factory = FoodQuestionnaireViewModelFactory(context))
    val coroutineScope = rememberCoroutineScope()

    //user ID list from Room DB
    val userIds by viewModel.userIds.collectAsState()

    //sharedprefs for storing login session
    val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    //input states
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(LoginMode.LOGIN) }
    var selectedUserId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    //error flags
    var userIdError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var passwordMismatchError by remember { mutableStateOf(false) }
    var unclaimedAccountError by remember { mutableStateOf(false) }
    var loginFailed by remember { mutableStateOf(false) }

    //reset input fields and errors
    fun resetFields() {
        selectedUserId = ""
        phone = ""
        password = ""
        confirmPassword = ""
        name = ""
        userIdError = false
        phoneError = false
        passwordError = false
        passwordMismatchError = false
        unclaimedAccountError = false
        loginFailed = false
    }

    //load user IDs when screen is first loaded
    LaunchedEffect(Unit) {
        viewModel.loadUserIds()
    }

    //check if selected user ID is already claimed in register mode
    LaunchedEffect(selectedUserId, mode) {
        if (mode == LoginMode.REGISTER && selectedUserId.isNotBlank()) {
            val patient = viewModel.getPatientById(selectedUserId)
            unclaimedAccountError = patient?.password?.isNotBlank() == true
        } else {
            unclaimedAccountError = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {

                    //collapse dropdown + clear focus
                    focusManager.clearFocus()
                    isDropdownExpanded = false
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (mode == LoginMode.LOGIN) "NutriTrack Login" else "Claim Your Account",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 28.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                //user ID Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedUserId,
                        onValueChange = {},
                        readOnly = true,
                        isError = userIdError || unclaimedAccountError,
                        label = { Text("Select User ID") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null,
                                modifier = Modifier.clickable { isDropdownExpanded = true })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        userIds.sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }.forEach { id ->
                            DropdownMenuItem(
                                text = { Text(id) },
                                onClick = {
                                    selectedUserId = id
                                    isDropdownExpanded = false
                                    userIdError = false
                                }
                            )
                        }
                    }
                }


                if (userIdError) {
                    Text("Please select a User ID", color = MaterialTheme.colorScheme.error)
                } else if (unclaimedAccountError && mode == LoginMode.REGISTER) {
                    Text("This ID has already been claimed", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (mode == LoginMode.REGISTER) {

                    //name input (optional)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    //phone number input
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                phone = it
                                phoneError = false
                            }
                        },
                        isError = phoneError,
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (phoneError) {
                        Text("Phone number is incorrect or empty", color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                //password input
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = false
                        loginFailed = false
                    },
                    isError = passwordError || loginFailed,
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (passwordError) {
                    Text("Password must be at least 8 characters", color = MaterialTheme.colorScheme.error)
                } else if (loginFailed) {
                    Text("Wrong password. Please try again", color = MaterialTheme.colorScheme.error)
                }

                if (mode == LoginMode.REGISTER) {
                    Spacer(modifier = Modifier.height(16.dp))

                    //confirm password input
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            passwordMismatchError = false
                        },
                        isError = passwordMismatchError,
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    if (passwordMismatchError) {
                        Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "This app is only for pre-registered users. Please enter your ID and password or Register to claim your account.",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp, start = 12.dp, end = 12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                //login/register button
                Button(
                    onClick = {
                        userIdError = selectedUserId.isBlank()
                        phoneError = (mode == LoginMode.REGISTER) && phone.isBlank()
                        passwordError = password.length < 8
                        passwordMismatchError = (mode == LoginMode.REGISTER) && (password != confirmPassword)

                        if (userIdError || phoneError || passwordError || passwordMismatchError) return@Button

                        coroutineScope.launch {
                            val patient = viewModel.getPatientById(selectedUserId)

                            if (mode == LoginMode.LOGIN) {
                                if (patient == null || patient.password.isNullOrEmpty()) {
                                    unclaimedAccountError = true
                                    Toast.makeText(context, "This account has not been claimed yet", Toast.LENGTH_SHORT).show()
                                } else if (patient.password != password) {
                                    loginFailed = true
                                } else {
                                    prefs.edit(commit = true) { putString("userId", selectedUserId) }
                                    val hasFilled = foodQuestionnaireViewModel.hasUserFilled(selectedUserId)
                                    navController.navigate(if (hasFilled) "home" else "questionnaire/$selectedUserId")
                                }
                            } else {
                                if (patient == null) {
                                    unclaimedAccountError = true
                                    Toast.makeText(context, "Invalid ID", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                if (!patient.password.isNullOrEmpty()) {
                                    unclaimedAccountError = true
                                    Toast.makeText(context, "This ID has already been claimed", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                if (patient.phoneNumber != phone) {
                                    phoneError = true
                                    Toast.makeText(context, "Phone number does not match our records", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                viewModel.claimAccount(
                                    userId = selectedUserId,
                                    name = if (name.isBlank()) null else name,
                                    phone = phone,
                                    password = password
                                )

                                var claimedPatient: Patient? = null
                                repeat(5) {
                                    delay(100L)
                                    claimedPatient = viewModel.getPatientById(selectedUserId)
                                    if (claimedPatient != null) return@repeat
                                }

                                if (claimedPatient != null) {
                                    prefs.edit { putString("userId", selectedUserId) }
                                    Toast.makeText(context, "Account claimed successfully", Toast.LENGTH_SHORT).show()
                                    val hasFilled = foodQuestionnaireViewModel.hasUserFilled(selectedUserId)
                                    navController.navigate(if (hasFilled) "home" else "questionnaire/$selectedUserId")
                                } else {
                                    Toast.makeText(context, "Account creation failed. Try again.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    enabled = !(mode == LoginMode.REGISTER && unclaimedAccountError), // disables Register if ID is claimed
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (mode == LoginMode.LOGIN) "Login" else "Register")
                }


                Spacer(modifier = Modifier.height(16.dp))

                //toggle between login and register
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        mode = if (mode == LoginMode.LOGIN) LoginMode.REGISTER else LoginMode.LOGIN
                        resetFields()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (mode == LoginMode.LOGIN) "Claim Account" else "Back to Login")
                }
            }
        }
    }
}
