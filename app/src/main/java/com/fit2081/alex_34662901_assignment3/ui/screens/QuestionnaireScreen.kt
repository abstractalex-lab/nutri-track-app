package com.fit2081.alex_34662901_assignment3.ui.screens

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fit2081.alex_34662901_assignment3.data.AppDatabase
import com.fit2081.alex_34662901_assignment3.data.foodquestionnaire.FoodQuestionnaire
import com.fit2081.alex_34662901_assignment3.data.foodquestionnaire.FoodQuestionnaireViewModel
import com.fit2081.alex_34662901_assignment3.data.foodquestionnaire.FoodQuestionnaireViewModelFactory
import kotlinx.coroutines.launch
import java.util.*

/**
 * Composable function QuestionnaireScreen handles the food intake form in NutriTrack
 *
 * This screen allows users to:
 * - Select food categories they consume
 * - View and select a health persona
 * - Input approximate times for meal, sleep, and wake
 *
 * On submit, the data is saved to Room via FoodQuestionnaireViewModel.
 * Validation features prevent users from entering duplicate time values and provides error indicators.
 *
 * @param navController used to navigate to home or login
 * @param userId identifies the logged-in patient
 *
 */
@SuppressLint("DiscouragedApi")
@Composable
fun QuestionnaireScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val viewModel: FoodQuestionnaireViewModel = viewModel(factory = FoodQuestionnaireViewModelFactory(context))
    val previousRoute = remember {
        navController.previousBackStackEntry?.destination?.route
    }
    val coroutineScope = rememberCoroutineScope()

    //define list of food and persona options, descriptions shown when persona info button is clicked
    val foodOptions = listOf("Fruits", "Vegetables", "Grains", "Red Meat", "Seafood", "Poultry", "Fish", "Eggs", "Nuts/Seeds")
    val personaOptions = listOf("Health Devotee", "Mindful Eater", "Wellness Striver", "Balance Seeker", "Health Procrastinator", "Food Carefree")
    val personaDescriptions = mapOf(
        "Health Devotee" to "I'm passionate about healthy eating & health plays a big part in my life. " +
                "I use social media to follow active lifestyle personalities or get new recipes/exercise ideas. I may even buy superfoods or follow a particular type of diet. I like to think I am super healthy.",
        "Mindful Eater" to "I'm health-conscious and being healthy and eating healthy is important to me. " +
                "Although health means different things to different people, I make conscious lifestyle decisions about eating based on what I believe healthy means. I look for new recipes and healthy eating information on social media.",
        "Wellness Striver" to "I aspire to be healthy (but struggle sometimes). Healthy eating is hard work! " +
                "I’ve tried to improve my diet, but always find things that make it difficult to stick with the changes. Sometimes I notice recipe ideas or healthy eating hacks, and if it seems easy enough, I’ll give it a go.",
        "Balance Seeker" to "I try and live a balanced lifestyle, and I think that all foods are okay in moderation. " +
                "I shouldn’t have to feel guilty about eating a piece of cake now and again. I get all sorts of inspiration from social media like finding out about new restaurants, fun recipes and sometimes healthy eating tips.",
        "Health Procrastinator" to "I’m contemplating healthy eating but it’s not a priority for me right now. " +
                "I know the basics about what it means to be healthy, but it doesn’t seem relevant to me right now. I have taken a few steps to be healthier but I am not motivated to make it a high priority because I have too many other things going on in my life.",
        "Food Carefree" to "I’m not bothered about healthy eating. I don’t really see the point and " +
                "I don’t think about it. I don’t really notice healthy eating tips or recipes and I don’t care what I eat."
    )

    //form state variables
    var dropdownPersona by remember { mutableStateOf("") }
    var mealTime by remember { mutableStateOf("") }
    var sleepTime by remember { mutableStateOf("") }
    var wakeTime by remember { mutableStateOf("") }

    //track selected food options
    val selectedFoods = remember {
        mutableStateMapOf<String, Boolean>().apply {
            foodOptions.forEach { this[it] = false }
        }
    }
    val showTimePickerFor = remember { mutableStateOf("") }
    var personaDropdownExpanded by remember { mutableStateOf(false) }
    var dialogPersona by remember { mutableStateOf<String?>(null) }

    //field-specific error flags
    var showErrors by remember { mutableStateOf(false) }
    val foodError by remember { derivedStateOf { showErrors && selectedFoods.values.none { it } } }
    var mealTimeError by remember { mutableStateOf(false) }
    var sleepTimeError by remember { mutableStateOf(false) }
    var wakeTimeError by remember { mutableStateOf(false) }


    //pre-fill fields from Room if user has previous data
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val existing = viewModel.getByUserId(userId)
            existing?.let {
                dropdownPersona = it.persona
                mealTime = it.mealTime
                sleepTime = it.sleepTime
                wakeTime = it.wakeTime
                it.selectedFoods.split(",").forEach { food ->
                    selectedFoods[food.trim()] = true
                }
            }
        }
    }

    //handle physical back press
    BackHandler {
        if (previousRoute?.startsWith("home") == true) {
            navController.popBackStack("home", inclusive = false)
        } else {
            navController.popBackStack("login", inclusive = false)
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

                    //clear dropdowns and focus when clicking outside
                    focusManager.clearFocus()
                    personaDropdownExpanded = false
                }
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopBar(
                    title = "Food Intake Questionnaire",
                    onBackClick = {
                        if (previousRoute?.startsWith("home") == true) {
                            navController.popBackStack("home", inclusive = false)
                        } else {
                            navController.popBackStack("login", inclusive = false)
                        }
                    },
                    onLogoutClick = {
                        Toast.makeText(context, "Successfully logged out!", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),

                    //for nav bar clearance
                    contentPadding = PaddingValues(bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    item {
                        Text("Tick all the food categories you can eat:", fontWeight = FontWeight.Bold,
                            color = if (foodError) MaterialTheme.colorScheme.error else LocalContentColor.current)
                    }

                    //setup checkboxes with 2 per row, import from foodOptions val
                    items(foodOptions.chunked(2)) { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            rowItems.forEach { item ->

                                //setup each cell behaviour, highlight red if none are checked
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = selectedFoods[item] == true,
                                        onCheckedChange = { selectedFoods[item] = it },
                                        colors = CheckboxDefaults.colors(
                                            uncheckedColor = if (foodError) MaterialTheme.colorScheme.error else Color.Unspecified
                                        )
                                    )
                                    Text(
                                        text = item,
                                        color = if (foodError) MaterialTheme.colorScheme.error else LocalContentColor.current
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    //setup persona information buttons
                    item {
                        Text("Your Persona", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                            //2 buttons information each row, when press pulls up an information screen
                            personaOptions.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { persona ->
                                        OutlinedButton(
                                            onClick = { dialogPersona = persona },
                                            modifier = Modifier.weight(1f),
                                            border = BorderStroke(1.dp, Color.Gray)
                                        ) {
                                            Text(
                                                persona,
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    //setup drop-down persona selection
                    item {
                        Text("Which persona best fits you?", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        //box allow inputs from both persona and drop-down, highlight red if unselected
                        Box(Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                isError = showErrors && dropdownPersona.isBlank(),
                                value = dropdownPersona,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Persona") },
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable {
                                        personaDropdownExpanded = true
                                    })
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { personaDropdownExpanded = true }
                            )

                            //drop-down menu behaviour
                            DropdownMenu(
                                expanded = personaDropdownExpanded,
                                onDismissRequest = { personaDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                //loops through each persona option and display them
                                personaOptions.forEach {
                                    DropdownMenuItem(
                                        text = { Text(it) },
                                        onClick = {
                                            dropdownPersona = it
                                            personaDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    //setup timings section
                    item {
                        Text("Timings", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        //trigger the time selection dialog
                        if (showTimePickerFor.value.isNotEmpty()) {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(context, { _, hour, min ->
                                val t = "%02d:%02d".format(hour, min)
                                when (showTimePickerFor.value) {
                                    "meal" -> mealTime = t
                                    "sleep" -> sleepTime = t
                                    "wake" -> wakeTime = t
                                }
                                showTimePickerFor.value = ""
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).apply {
                                setOnCancelListener { showTimePickerFor.value = "" }
                            }.show()
                        }

                        /**
                         * Composable function timeField streamlining the time entry for each section
                         * Field(s) is read-only, and highlight red if no time input(s) was found
                         *
                         * @param label
                         * @param value
                         * @param isError
                         * @param onClick
                         * @return
                         *
                         */
                        @Composable
                        fun timeField(label: String, value: String, isError: Boolean, onClick: () -> Unit) {
                            OutlinedTextField(
                                value = value,
                                readOnly = true,
                                onValueChange = {},
                                isError = isError,
                                label = { Text(label) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Time Picker",
                                        modifier = Modifier.clickable(onClick = onClick)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        //invoke the timeField function to trigger time dialogue upon clicking the box
                        timeField("What time of day approx. do you normally eat your biggest meal?", mealTime, showErrors && mealTime.isBlank() || mealTimeError) {
                            showTimePickerFor.value = "meal"
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        timeField("What time of day approx. do you go to sleep at night?", sleepTime, showErrors && sleepTime.isBlank() || sleepTimeError) {
                            showTimePickerFor.value = "sleep"
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        timeField("What time of day approx. do you wake up in the morning?", wakeTime, showErrors && wakeTime.isBlank() || wakeTimeError) {
                            showTimePickerFor.value = "wake"
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    //setup submit behaviour
                    item {
                        Button(
                            onClick = {
                                showErrors = true
                                mealTimeError = false
                                sleepTimeError = false
                                wakeTimeError = false

                                //initiate showing errors on any invalid entry fields
                                val noFoodSelected = selectedFoods.values.none { it }
                                if (dropdownPersona.isBlank() || mealTime.isBlank() || sleepTime.isBlank() || wakeTime.isBlank() || noFoodSelected) {
                                    Toast.makeText(context, "Please complete all required fields.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                //check for any duplicated time
                                if (mealTime == sleepTime) {
                                    mealTimeError = true
                                    sleepTimeError = true
                                }
                                if (mealTime == wakeTime) {
                                    mealTimeError = true
                                    wakeTimeError = true
                                }
                                if (sleepTime == wakeTime) {
                                    sleepTimeError = true
                                    wakeTimeError = true
                                }

                                //block submit if any error
                                if (mealTimeError || sleepTimeError || wakeTimeError) {
                                    Toast.makeText(context, "Meal, Sleep, and Wake times must be different", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                coroutineScope.launch {

                                    //get DAO reference from Room, check if patient exists
                                    val patientDao = AppDatabase.getDatabase(context).patientDao()
                                    val exists = patientDao.getPatientById(userId) != null

                                    if (!exists) {

                                        //show error if patient record not found (data corruption or manual tampering)
                                        Toast.makeText(context, "Error: Patient record not found. Please re-login.", Toast.LENGTH_LONG).show()

                                        //navigate to login and clear stack
                                        navController.navigate("login") {
                                            navController.navigate("login") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                        }
                                        return@launch
                                    }

                                    //store questionnaire response into Room DB
                                    viewModel.insertOrUpdate(
                                        FoodQuestionnaire(
                                            userId = userId,
                                            persona = dropdownPersona,
                                            mealTime = mealTime,
                                            sleepTime = sleepTime,
                                            wakeTime = wakeTime,
                                            selectedFoods = selectedFoods.filterValues { it }.keys.joinToString(", ")
                                        )
                                    )

                                    //navigate to home and clear back stack
                                    navController.navigate("home") {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }

            //modal dialog for more information on personas
            if (dialogPersona != null) {
                val persona = dialogPersona
                AlertDialog(

                    //dismiss either on button or anywhere outside box
                    onDismissRequest = { dialogPersona = null },
                    title = {
                        Text(persona.toString(), fontWeight = FontWeight.Bold) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            //shows image of persona with name matches in snake_case (naming convention is persona_name), paste the text based on the mapping description
                            Image(
                                painter = painterResource(id = context.resources.getIdentifier(persona.toString().lowercase().replace(" ", "_"), "drawable", context.packageName)),
                                contentDescription = persona,
                                modifier = Modifier.size(120.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(personaDescriptions[persona] ?: "")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                dropdownPersona = persona ?: ""
                                dialogPersona = null
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { dialogPersona = null }) {
                            Text("Dismiss")
                        }
                    }
                )
            }
        }
    }
}


/**
 * Composable function TopBar displays back button and overflow menu with logout. Kebab menu for logout, and dynamic back button
 *
 * @param title screen title
 * @param onBackClick triggered when back is pressed
 * @param onLogoutClick triggered when logout is selected
 */
@Composable
fun TopBar(title: String, onBackClick: () -> Unit, onLogoutClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            start = 16.dp,
            end = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Logout") },
                    onClick = {
                        menuExpanded = false
                        onLogoutClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout Icon",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
            }
        }
    }
}
