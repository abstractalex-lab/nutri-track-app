package com.fit2081.alex_34662901_assignment3

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fit2081.alex_34662901_assignment3.data.AppDatabase
import com.fit2081.alex_34662901_assignment3.data.seed.CsvSeeder
import com.fit2081.alex_34662901_assignment3.ui.screens.HomeScreen
import com.fit2081.alex_34662901_assignment3.ui.screens.LoginScreen
import com.fit2081.alex_34662901_assignment3.ui.screens.QuestionnaireScreen
import com.fit2081.alex_34662901_assignment3.ui.theme.Alex_34662901_Assignment3Theme
import kotlinx.coroutines.launch

/**
 * MainActivity serves as the entry point for the NutriTrack app
 *
 * This screen performs the following:
 * - Seeds the Room database from CSV files on first launch using CsvSeeder
 * - Applies the global theme and initializes navigation
 * - Checks SharedPreferences to determine if a user is already logged in and sets the start screen accordingly (either Welcome or Home)
 *
 * This screen allows users to:
 * - Understand the purpose of the app
 * - Refers to an accredited practitioner in regards to the topic
 * - Navigate to login screen
 *
 * Navigation graph handles routing between:
 * - Welcome screen
 * - Login screen
 * - Questionnaire screen
 * - Home screen
 *
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //seed Room DB with CSV data on first launch
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            CsvSeeder(applicationContext, db).seedIfNeeded()
        }

        //enable full-screen layout
        enableEdgeToEdge()
        setContent {
            Alex_34662901_Assignment3Theme { 
                val navController = rememberNavController()

                //check if a user is already logged in via SharedPreferences
                val prefs = applicationContext.getSharedPreferences("login_prefs", MODE_PRIVATE)
                val isLoggedIn = !prefs.getString("userId", "").isNullOrEmpty()

                //determine the initial screen to show
                val initialRoute = if (isLoggedIn) "home" else "welcome"

                Surface {

                    //define navigation graph
                    NavHost(navController = navController, startDestination = initialRoute) {
                        composable("welcome") { WelcomeScreen(navController) }
                        composable("login") { LoginScreen(navController) }
                        composable("questionnaire/{userId}") { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            QuestionnaireScreen(navController, userId)
                        }
                        composable("home") { HomeScreen(navController) }
                    }
                }
            }
        }
    }
}


/**
 * Composable function DisclaimerScreenContent to display disclaimer
 *
 * @param navController navigation controller used for navigating to LoginScreen
 */
@Composable
fun WelcomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as? Activity

    //handle physical back button press to close the app
    BackHandler {
        activity?.finish()
    }

    //setup ui container and center layout
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "NutriTrack", fontSize = 50.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Image(
                painter = painterResource(id = R.drawable.nutritrack_logo),
                contentDescription = "App intro logo",
                modifier = Modifier
                    .size(125.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(25.dp))

            //invoke the function to display disclaimer text with hyperlink
            AnnotatedHtmlStringWithLink()
            Spacer(modifier = Modifier.height(50.dp))

            //button navigate to LoginScreen
            Button(
                onClick = { navController.navigate("login") }
            ) {
                Text("Login Here")
            }

            Spacer(modifier = Modifier.height(50.dp))

            //information text student Name + ID
            Text(
                text = "Alex Bui (34662901)",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}


/**
 * Composable function for writing disclaimer with link redirects to accredited practitioner
 *
 * @param modifier
 * @param htmlText
 * @credit https://developer.android.com/develop/ui/compose/text/style-text#display_html_with_links_in_text
 */
@Composable
fun AnnotatedHtmlStringWithLink(
    modifier: Modifier = Modifier, htmlText: String = """
   <p style="font-family:sans-serif">
       This app provides general health and nutrition information for educational purposes only. 
       It is not intended as medical advice, diagnosis, or treatment.<br>
       Always consult a qualified healthcare professional before making any changes to your diet, 
       exercise, or health regimen.<br>
       If you’d like to an Accredited Practicing Dietitian (APD), please
       visit the
       <a href="https://www.monash.edu/medicine/scs/nutrition/clinics/nutrition">Monash Nutrition/Dietetics Clinic (discounted rates for
       students).</a>
   </p>
""".trimIndent()
) {
    Text(

        //text styling for the hyperlink
        AnnotatedString.fromHtml(
            htmlText,
            linkStyles = TextLinkStyles(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    fontStyle = FontStyle.Italic,
                    color = Color.Blue,
                    fontFamily = FontFamily.Default
                )
            )
        ),

        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        textAlign = TextAlign.Center,
    )
}