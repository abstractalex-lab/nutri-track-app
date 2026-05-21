# NutriTrack 🥗

> *A personalised Android nutrition insights platform — because eating well shouldn't be complicated.*

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Room_DB-FF6F00?style=flat&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase_AI_Logic-FFCA28?style=flat&logo=firebase&logoColor=black)
![Status](https://img.shields.io/badge/status-maintained-brightgreen?style=flat)


## ℹ️ Overview

NutriTrack is an Android application that gives users a personalised window into their dietary health. Built on top of a simple nutrition questionnaire app, this version transforms it into a fully-featured platform: persistent multi-user accounts, real-time third-party API data, AI-generated coaching, and a clinician-facing analytics dashboard.

The app scores users against the **HEIFA (Healthy Eating Index for Australians)** framework across multiple food categories and uses those scores to drive personalised recommendations via the Gemini model, integrated through **Firebase AI Logic**.

This was completed as **Assignment 3 (30%)** for **FIT2081 – Mobile App Development** at Monash University, Semester 1, 2025. It was a solo project targeting HD (High Distinction) marks.

### 🌟 Highlights

- 🔐 **Multi-user authentication** with persistent sessions and secure password management
- 🤖 **AI-powered NutriCoach** using Gemini via Firebase AI Logic, personalised to each user's HEIFA score profile
- 🍓 **Live fruit nutrition data** via the FruityVice API, conditionally shown based on dietary scores
- 🏥 **Clinician Admin Dashboard** with passphrase-protected access and AI-generated data insights
- 🗄️ **Room database** seeded once from CSV — all subsequent reads/writes are local and persistent
- 📋 **Double-tap to copy** AI tips straight to clipboard (original HD feature)

### ✍️ Author

**Alex Bui** — Monash University, FIT2081 (S1 2025)
Student ID: 34662901 | Lab: 09_OnCampus | [GitHub](https://github.com/alexbui)


## ⬇️ Getting Started

> ℹ️ Originally submitted as a university project, now actively maintained and improved post-submission.

**Requirements:** Android Studio Hedgehog or later, Android API 30+

```bash
git clone https://github.com/alexbui/nutri-track-app.git
```

1. Open the project in **Android Studio** and let Gradle sync
2. Set up a [Firebase project](https://console.firebase.google.com) and register your Android app with package name `com.alexbui.nutritrack`
3. Download `google-services.json` from Firebase and place it in the `app/` directory
4. Enable **Firebase AI Logic** in your Firebase project console and select the **Gemini Developer API** (free tier)
5. **Build → Clean Project**, then **Build → Rebuild Project**
6. Run on an emulator or physical device (API 30+)

The Room database seeds automatically from the bundled CSV on first launch — no manual setup needed.

> **Note:** No API key setup is required. Authentication is handled automatically by Firebase via `google-services.json`.
> The active Gemini model is managed remotely via **Firebase Remote Config** (`gemini_model_name`) — update it anytime from the Firebase console without redeploying the app.


## 🚀 Features

### 🔐 Authentication & Session Management
- First-time account claim flow: users verify via UserID + phone number, then set a name and password
- Credentials stored securely in Room; subsequent logins only require UserID + password
- Session persists across app restarts until the user manually logs out
- **Change Password** — verifies current password before updating, with field-level validation

### 🤖 NutriCoach Assistant
- AI motivational tips generated via **Gemini** (through Firebase AI Logic), enriched with the user's full HEIFA score data for specificity
- Model name managed dynamically via **Firebase Remote Config** — no redeployment needed to switch models
- Fruit section powered by the **FruityVice API** — shown only when the user's fruit HEIFA score is non-optimal
- Fruit search includes a loading indicator and a styled warning card for invalid fruit names
- When fruit score is optimal, a random image loads from [Picsum Photos](https://picsum.photos/) instead
- Every AI tip is stored in the `NutriCoachTips` table and viewable in a scrollable modal history
- **Double-tap any tip to copy it to clipboard** — with snackbar confirmation

### 📊 Nutrition Insights
- HEIFA score breakdown across all tracked food categories with progress bars
- Shareable score summary via Android share sheet
- Reactive UI driven by LiveData and ViewModel — no manual refresh needed

### ⚙️ Settings Screen
- Displays current user's name, phone number, and ID
- Logout with confirmation dialog and redirect to login screen
- Change Password and Clinician Login gateway

### 🏥 Clinician / Admin Dashboard *(HD)*
- Passphrase-protected admin view
- Average HEIFA scores broken down by sex (male / female)
- 3 Gemini-generated insights based on patterns across the full patient database


## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM — ViewModel + Repository + LiveData |
| Local Database | Room (SQLite) |
| Networking | Retrofit + Coroutines |
| AI Integration | Firebase AI Logic (`com.google.firebase:firebase-ai`) |
| AI Model | Gemini 2.5 Flash Lite (via Gemini Developer API, free tier) |
| Model Management | Firebase Remote Config |
| Fruit Data | FruityVice API |
| Random Images | Picsum Photos |


## 🏗️ Architecture

The app follows **MVVM architecture** throughout, with all data flowing through the Repository layer:

```
UI Layer (Jetpack Compose Screens)
        ↕  observe via LiveData / StateFlow
    ViewModel
        ↕
    Repository
        ↕                    ↕
Room Database (local)    Retrofit (remote APIs)
```

On first launch only, patient and food intake data is seeded from a bundled CSV into Room. After that, the CSV is never accessed again.

**Database tables:**

| Table | Contents |
|---|---|
| `Patient` | UserID, PhoneNumber, Name, Sex, HEIFA scores |
| `FoodIntake` | Questionnaire responses (FK → Patient) |
| `NutriCoachTips` | AI-generated tips per user |


## 📁 Project Structure

```
com/alexbui/nutritrack/
├── data/
│   ├── foodquestionnaire/    # FoodQuestionnaire entity, DAO, ViewModel
│   ├── nutricoach/           # NutriCoachTip, FruityVice API, ViewModel
│   ├── patient/              # Patient entity, DAO, ViewModel
│   ├── seed/                 # CSV seeder, SeedFlag
│   └── AppDatabase.kt        # Room database instance
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── QuestionnaireScreen.kt
│   │   ├── ClinicalScreen.kt
│   │   ├── ChangePasswordScreen.kt
│   │   ├── GenAIViewModel.kt
│   │   └── GenAIUiState.kt
│   └── theme/
└── MainActivity.kt
```


## 🏆 HD Features

Two original features were submitted beyond the base specification for HD/HD++ consideration:

1. **Double-tap to copy AI tips** (`HomeScreen.kt`)
   Users can double-tap any tip in the history modal to copy it to their clipboard. A snackbar confirms the action. Improves UX for users who want to save or share their personalised coaching messages.

2. **Change Password** (`ChangePasswordScreen.kt`)
   Accessible from the Settings screen. Verifies the user's current password before accepting a new one, with confirmation matching and length validation. Updates credentials directly in Room.


## 💭 Academic Context & Disclaimer

This project was submitted for academic assessment at Monash University. It is shared here for portfolio and reference purposes only.

> Please respect Monash University's academic integrity policy — **do not submit this work or any derivative as your own.**

If you're a fellow student and found this useful as a reference, consider opening a [Discussion](../../discussions) — always happy to talk about Android dev!