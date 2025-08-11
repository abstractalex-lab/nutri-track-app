package com.fit2081.nutri_track_app.ui.screens

import android.annotation.SuppressLint
import android.content.*
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.fit2081.nutri_track_app.data.AppDatabase
import com.fit2081.nutri_track_app.data.foodquestionnaire.FoodQuestionnaire
import com.fit2081.nutri_track_app.data.nutricoach.*
import com.fit2081.nutri_track_app.data.patient.Patient
import com.fit2081.nutri_track_app.R
import com.fit2081.nutri_track_app.ui.theme.DeepGreen

/**
 * Composable function HomeScreen serves as the main entry point for authenticated users
 *
 * It embeds a nested NavHost with four tabs:
 * - Home
 * - Insights
 * - NutriCoach
 * - Settings
 *
 * Handles back button behavior to close the app if on Home tab.
 *
 * @param appNavController outer navigation controller for app-wide navigation
 *
 */
@Composable
fun HomeScreen(appNavController: NavHostController) {
    val context = LocalContext.current
    val navControllerInner = rememberNavController()
    val currentRoute by navControllerInner.currentBackStackEntryAsState()
    val isOnHomeTab = currentRoute?.destination?.route == "home"

    //scaffold with bottom navigation bar
    Scaffold(
        bottomBar = {
            BottomBar(navControllerInner)
        }
    ) { innerPadding ->

        //nav host for inner tabs
        NavHost(
            navController = navControllerInner,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeContent(
                    innerNavController = navControllerInner,
                    appNavController = appNavController
                )
            }

            composable("insights") {
                InsightsContent(
                    innerNavController = navControllerInner,
                    appNavController = appNavController
                )
            }

            composable("nutricoach") {
                NutriCoachContent()
            }

            composable("settings") {
                SettingsContent(
                    innerNavController = navControllerInner,
                    appNavController = appNavController
                )
            }

            composable("clinician_login") {
                ClinicianLoginScreen(navController = navControllerInner)
            }

            composable("clinician_dashboard") {
                ClinicianDashboardScreen(navController = navControllerInner)
            }

            composable("change_password") {
                ChangePasswordScreen(
                    navController = navControllerInner,
                    userId = LocalContext.current
                        .getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                        .getString("userId", "") ?: ""
                )
            }
        }
    }

    //exit app when back pressed on home tab
    BackHandler(enabled = isOnHomeTab) {
        (context as? ComponentActivity)?.finishAffinity()
    }
}


/**
 * Composable function BottomBar for handling displays the navigation bar with 4 tabs:
 * Home, Insights, NutriCoach, and Settings
 *
 * @param navController controller used to track current tab and navigate
 *
 */
@Composable
fun BottomBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    //define button routes, icons, and labels
    val items = listOf(
        "home" to Pair(Icons.Default.Home, "Home"),
        "insights" to Pair(Icons.Default.Info, "Insights"),
        "nutricoach" to Pair(Icons.Default.Star, "NutriCoach"),
        "settings" to Pair(Icons.Default.Settings, "Settings")
    )

    NavigationBar {
        items.forEach { (route, item) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {

                    //navigate without duplicating stack
                    navController.navigate(route) {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                icon = { Icon(item.first, contentDescription = item.second) },
                label = { Text(item.second) }
            )
        }
    }
}


/**
 * Composable function HomeContent for displaying:
 * - Welcome message with name or ID
 * - HEIFA total score
 * - Edit Questionnaire button
 * - Meal composition image
 * - Button to navigate to insights
 *
 * @param innerNavController inner tab navigation controller
 * @param appNavController app-wide navigation controller
 *
 */
@Composable
fun HomeContent(innerNavController: NavHostController, appNavController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getString("userId", "") ?: ""
    var patient: Patient? by remember { mutableStateOf(null) }

    //load patient data from DB on screen entry
    LaunchedEffect(userId) {
        val db = AppDatabase.getDatabase(context)
        val loadedPatient = db.patientDao().getPatientById(userId)
        Log.d("PatientCheck", "Loaded for ID $userId: $loadedPatient")
        patient = loadedPatient
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {

        //greeting header
        Text("Hello,", fontSize = 15.sp)
        Text(
            text = patient?.name?.takeIf { it.isNotBlank() } ?: "User $userId",
            fontSize = 26.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        //edit button + caption
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "You’ve already filled in your Food Intake Questionnaire, but you can change details here:",
                fontSize = 14.sp,
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    appNavController.navigate("questionnaire/$userId")
                },
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
            ) {
                Text("Edit", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        //sample image representing food composition, courtesy of Google https://images.app.goo.gl/PxAgosPjsjGNHLYw7
        Image(
            painter = painterResource(id = R.drawable.meal_compo),
            contentDescription = "Meal Composition",
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Your Total Food Quality Score", fontSize = 20.sp)

        Text(
            text = patient?.heifaTotalScore?.let { "$it / 100" } ?: "Loading...",
            fontSize = 28.sp,
            color = DeepGreen
        )

        Spacer(modifier = Modifier.height(12.dp))

        //view detailed score button navigates to Insights composable screen
        Button(
            onClick = {

                //prevents multiple entries, only home top
                innerNavController.navigate("insights") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            },
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("View Detailed Score", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(48.dp))
        Text("What is the Food Quality Score?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Your Food Quality Score provides a snapshot of how well your eating patterns align with established food guidelines, " +
                    "helping you identify both strengths and opportunities for improvement in your diet.\n\n" +
                    "This personalized measurement considers various food groups including vegetables, fruits, whole grains, " +
                    "and proteins to give you practical insights for making healthier food choices.",
            fontSize = 13.sp,
            lineHeight = 16.sp
        )
    }
}


/**
 * Composable function InsightsContent shows detailed breakdown of HEIFA component scores
 *
 * Features:
 * - Linear progress bars per food group
 * - Overall HEIFA score
 * - Share button
 * - Navigate to NutriCoach for help
 *
 * @param innerNavController inner tab nav controller
 * @param appNavController
 *
 */
@SuppressLint("DefaultLocale")
@Composable
fun InsightsContent(innerNavController: NavHostController, appNavController: NavHostController) {
    val context = LocalContext.current
    val userId = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        .getString("userId", "") ?: ""
    var patient by remember { mutableStateOf<Patient?>(null) }

    //build list of (label, score, max)
    val scores = patient?.let {
        listOf(
            Triple("Discretionary", it.discretionaryScore, 10f),
            Triple("Vegetables", it.vegetablesScore, 10f),
            Triple("Fruits", it.fruitsScore, 10f),
            Triple("Grains & Cereals", it.grainsCerealsScore, 5f),
            Triple("Whole Grains", it.wholeGrainsScore, 5f),
            Triple("Meat & Alternatives", it.meatAlternativesScore, 10f),
            Triple("Sodium", it.sodiumScore, 10f),
            Triple("Alcohol", it.alcoholScore, 5f),
            Triple("Dairy & Alternatives", it.dairyAlternativesScore, 10f),
            Triple("Water", it.waterScore, 5f),
            Triple("Sugar", it.sugarScore, 10f),
            Triple("Saturated Fat", it.saturatedFatScore, 5f),
            Triple("Unsaturated Fat", it.unsaturatedFatScore, 5f)
        )
    } ?: emptyList()
    val totalScore = patient?.heifaTotalScore ?: 0f

    //load patient on enter
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val db = AppDatabase.getDatabase(context)
            patient = db.patientDao().getPatientById(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Insights: Food Quality Score",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))

        //render component bars
        scores.forEach { (label, score, maxScore) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                //progress bar showing the component score
                LinearProgressIndicator(
                    progress = { (score / maxScore).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.LightGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                //format the progress number indicator to 2 decimal float (current) and 0 decimal float (total)
                Text(
                    text = String.format("%.2f / %.0f", score, maxScore),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
        }

        //total score summary displays in a progress bar-styled
        Text("Total Food Quality Score", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(

            //ensure score progress does not exceed no more than 100% (1 float)
            progress = { (totalScore / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.LightGray
        )

        Spacer(modifier = Modifier.height(4.dp))

        //format the progress number indicator to 2 decimal float (current)
        Text(
            String.format("%.2f / 100", totalScore),
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        //setup share button
        Button(
            onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"

                    //buildString constructs total score and loop through each component to append string into the shared text
                    val shareText = buildString {
                        append("Hi, I just got my Food Quality Score using NutriTrack!\n\n")
                        append("Total Score: ${"%.2f".format(totalScore)} / 100\n\n")
                        append("Component Breakdown:\n")
                        scores.forEach { (label, score, max) ->
                            append("- $label: ${"%.2f".format(score)} / ${"%.0f".format(max)}\n")
                        }
                        append("\nGenerated by NutriTrack App.")
                    }

                //set data to share, and start the activity chooser to share the text
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
                context.startActivity(Intent.createChooser(shareIntent, "Share my Food Quality Score"))
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Share with someone")
        }

        Spacer(modifier = Modifier.height(8.dp))

        //button navigate to NutriCoach composable to improve the diet
        Button(
            onClick = {
                innerNavController.navigate("nutricoach") {
                    popUpTo("home") { inclusive = false }
                    launchSingleTop = true
                }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Improve my diet!")
        }
    }
}


/**
 * Composable function NutriCoachContent gives users AI-driven tips and fruit info
 *
 * Features:
 * - Suggests fruit info using FruityVice API
 * - Displays user’s fruit score status
 * - Allows querying AI for encouragement (GenAIViewModel)
 * - Shows previously saved tips from NutriCoachTip table
 *
 */
@Composable
fun NutriCoachContent() {
    val context = LocalContext.current
    val aiViewModel = remember { GenAIViewModel() }
    val coachViewModel = remember { NutriCoachViewModel() }
    val uiState by aiViewModel.uiState.collectAsState()

    var questionnaire by remember { mutableStateOf<FoodQuestionnaire?>(null) }
    var fruitQuery by remember { mutableStateOf("") }

    val db = remember { AppDatabase.getDatabase(context) }
    var tipList by remember { mutableStateOf<List<NutriCoachTip>>(emptyList()) }
    var showTipsDialog by remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getString("userId", "") ?: ""
    var patient by remember { mutableStateOf<Patient?>(null) }

    //choose fruit score by sex, preset optimal of to min 8
    val fruitScore = patient?.fruitsScore ?: 0f
    val isFruitScoreOptimal = fruitScore >= 8f

    //load patient and questionnaire response from room DB
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val db = AppDatabase.getDatabase(context)
            patient = db.patientDao().getPatientById(userId)
            questionnaire = db.foodQuestionnaireDao().getByUserId(userId)
        }
    }

    //load saved tips if dialog opens
    LaunchedEffect(showTipsDialog) {
        if (showTipsDialog && userId.isNotEmpty()) {
            tipList = db.nutriCoachTipDao().getTipsForUser(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            "NutriCoach Assistant",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isFruitScoreOptimal) {

            //show motivation if fruit score is good
            AsyncImage(
                model = "https://picsum.photos/400/300",
                contentDescription = "Motivational Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Your fruit intake is excellent! Keep up the great work! 🍓🍍🍇")
        }

        else {

            //show input if fruit score needs work
            Text(
                "Let’s explore some fruits you can enjoy more of!",
                fontSize = 16.sp
            )

            //input field to type fruit name
            OutlinedTextField(
                value = fruitQuery.trim(),
                onValueChange = { fruitQuery = it },
                label = { Text("Type a fruit (e.g., apple, banana)") },
                modifier = Modifier.fillMaxWidth()
            )

            //button fetching fruit data info from FruityVice API
            Button(
                onClick = { coachViewModel.fetchFruitInfo(fruitQuery) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search Fruit Info")
            }

            //show error from API (e.g. fruit not found)
            coachViewModel.fruitError.value?.let {
                Text(it, color = Color.Red)
            }

            //show fruit nutrition data if API succeeds
            coachViewModel.fruitInfo.value?.let { info ->
                Text("Name: ${info.name}", fontWeight = FontWeight.Bold)
                Text("Family: ${info.family}")
                Text("Calories: ${info.nutritions.calories} cals")
                Text("Fat: ${info.nutritions.fat}g")
                Text("Carbs: ${info.nutritions.carbohydrates}g")
                Text("Sugar: ${info.nutritions.sugar}g")
                Text("Protein: ${info.nutritions.protein}g")
            }
        }
        Spacer(modifier = Modifier.height(2.dp))

        //divider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = Color.Gray
            )
            Text(
                "AI Assistant",
                modifier = Modifier.padding(horizontal = 8.dp),
                fontSize = 14.sp,
                color = Color.Gray
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        //ask AI and show past tips
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ){
            Button(
                onClick = {
                    patient?.let { p -> aiViewModel.sendPromptPatient(p, questionnaire, db) }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ask AI")
            }

            Button(
                onClick = { showTipsDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Past Tips")
            }
        }

        //ai response
        when (uiState) {
            is UIState.Loading -> {

                //loading animation while UIState at Loading
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Thinking...")
                }
            }
            is UIState.Success -> {
                Text(
                    text = (uiState as UIState.Success).result.trim(),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            is UIState.Error -> {
                Text("Error: ${(uiState as UIState.Error).message}", color = Color.Red)
            }
            else -> {}
        }

        //show past tips generated by AI
        if (showTipsDialog) {
            AlertDialog(
                onDismissRequest = { showTipsDialog = false },
                title = { Text("Previous AI Tips") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {

                        //show message if no tips found
                        if (tipList.isEmpty()) {
                            Text("No saved tips yet.")
                        } else {
                            Text(
                                text = "You can double tap on any tip to copy to the clipboard!",
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                fontSize = 13.sp,
                            )

                            //get clipboard manager to enable copy functionality
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                            //render each saved tip as a card
                            tipList.forEach { tip ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .pointerInput(tip) {
                                            detectTapGestures(
                                                onDoubleTap = {

                                                    //copy tip text to clipboard
                                                    val clip = ClipData.newPlainText("tip", tip.tipText)
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, "Tip copied! You can now paste on this phone", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(
                                        text = tip.tipText,
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 14.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }

                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTipsDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}


/**
 * Composable function SettingContent displays user information and settings actions
 *
 * Features:
 * - Shows name, phone, ID
 * - Change password
 * - Clinician login
 * - Logout with confirmation
 *
 * @param innerNavController inner tab navigation controller
 * @param appNavController app-wide navigation controller
 *
 */
@Composable
fun SettingsContent(innerNavController: NavHostController, appNavController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    val userId = prefs.getString("userId", "") ?: ""
    var patient by remember { mutableStateOf<Patient?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }


    //load user info from room DB
    LaunchedEffect(userId) {
        val db = AppDatabase.getDatabase(context)
        patient = db.patientDao().getPatientById(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        //account information section
        Text("ACCOUNT INFORMATION", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        InfoRow(icon = Icons.Default.Person, text = patient?.name ?: "N/A")
        InfoRow(icon = Icons.Default.Phone, text = patient?.phoneNumber?.let { "+$it" } ?: "N/A")
        InfoRow(icon = Icons.Default.Info, text = "ID: $userId")

        //divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        //settings section
        Text("SETTINGS", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        //navigate to clinician login screen
        ClickableSettingRow(
            icon = Icons.Default.Person,
            text = "Clinician Login"
        ) {
            //clinical login
            innerNavController.navigate("clinician_login")
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 12.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.background
        )

        //navigate to change password screen
        ClickableSettingRow(
            icon = Icons.Default.Lock,
            text = "Change Password"
        ) {
            innerNavController.navigate("change_password")
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 12.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.background
        )

        //logout button
        ClickableSettingRow(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            text = "Logout"
        ) {
            showLogoutDialog = true
        }

        //show dialogue confirming logout
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    TextButton(
                        onClick = {

                            //clear backstack, session and navigate to welcome screen
                            showLogoutDialog = false
                            Toast.makeText(context, "Successfully logged out!", Toast.LENGTH_SHORT).show()
                            prefs.edit { clear() }
                            appNavController.navigate("welcome") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        //app footer (for future uses)
        Text("Version 1.1.0", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(24.dp))
    }
}


/**
 * Composable function InfoRow displays a static row with an icon and corresponding text
 *
 * Used in Settings screen to show user info such as name, phone, or ID
 *
 * @param icon icon to display on the left
 * @param text string content to display next to the icon
 *
 */
@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        //display leading icon
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 16.sp)
    }
}


/**
 * Composable function ClickableSettingRow creates a tappable row used in the Settings menu
 *
 * Includes:
 * - Leading icon
 * - Setting label text
 * - Right arrow indicating it navigates
 *
 * @param icon leading icon to show
 * @param text label for the setting
 * @param onClick function triggered when row is clicked
 */
@Composable
fun ClickableSettingRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(16.dp))

            Text(text, fontSize = 16.sp, modifier = Modifier.weight(1f))

            //right arrow icon indicating navigation
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

