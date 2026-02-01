You are an expert Android engineer writing production-quality code.

PROJECT CONTEXT
- Android app written in Kotlin
- UI framework: Jetpack Compose (NO XML)
- Architecture: ViewModel + UI State (one-way data flow)
- Coroutines for timers and async logic
- No backend: all logic is local
- No global mutable state
- No blocking calls on main thread
- Clear separation of UI and business logic

====================================================
PROBLEM STATEMENT
====================================================
Build a passwordless authentication flow using Email + OTP,
followed by a Session screen that tracks login duration.

====================================================
FUNCTIONAL REQUIREMENTS
====================================================

1. EMAIL + OTP LOGIN
- User enters an email address
- User taps "Send OTP"
- A 6-digit numeric OTP is generated locally
- User enters OTP to log in
- OTP data must be stored per email

2. OTP RULES (STRICT)
- OTP length: exactly 6 digits
- OTP expiry: 60 seconds
- Maximum validation attempts: 3
- Generating a new OTP:
  - Invalidates the old OTP
  - Resets attempt count
- OTP logic must handle:
  - Expired OTP
  - Incorrect OTP
  - Exceeded attempts
  - Resend OTP

OTP data must be stored using a Map<String, OtpData>.

3. SESSION SCREEN
After successful login:
- Show session start time
- Show live session duration (mm:ss)
- Provide a Logout button

Timer requirements:
- Must survive recompositions
- Must stop correctly on logout
- Must use coroutine-based timing
- No while(true) loops without cancellation

4. EXTERNAL SDK (MANDATORY)
Integrate Timber logging.

- Add Timber dependency
- Initialize Timber in Application class
- Log events:
  - OTP generated
  - OTP validation success
  - OTP validation failure
  - Logout

====================================================
TECHNICAL EXPECTATIONS
====================================================

Jetpack Compose:
- @Composable functions
- remember vs rememberSaveable used correctly
- LaunchedEffect for side effects and timers
- Proper state hoisting
- Recomposition-safe logic

Architecture:
- ViewModel contains ONLY business logic
- UI observes immutable UI state
- No navigation or UI logic inside ViewModel
- One-way data flow: UI → ViewModel → State → UI

Kotlin / Data Structures:
- Sealed classes for UI state
- Data classes for state models
- Map for OTP storage
- Time handled using System.currentTimeMillis()

====================================================
PROJECT STRUCTURE (FOLLOW THIS)
====================================================

data/
- OtpManager.kt
  - Responsible ONLY for OTP generation, validation, expiry, attempts

viewmodel/
- AuthViewModel.kt
- AuthState.kt
  - Sealed UI states (EmailEntry, OtpSent, LoggedIn, Error)

ui/
- LoginScreen.kt
- OtpScreen.kt
- SessionScreen.kt

analytics/
- AnalyticsLogger.kt
  - Wrapper around Timber logging

====================================================
EDGE CASES
====================================================
- OTP expires after 60 seconds
- More than 3 invalid attempts fails OTP
- Resending OTP resets attempts and timer
- Screen rotation must not break:
  - OTP state
  - Session timer
- Logout clears all session state correctly

====================================================
DOCUMENTATION
====================================================
Generate a README.md explaining:
1. OTP logic and expiry handling
2. Data structures used and reasoning
3. Why Timber was chosen
4. What was AI-assisted vs manually understood and implemented

====================================================
IMPORTANT RULES
====================================================
- DO NOT use global mutable state
- DO NOT put business logic inside Composables
- DO NOT generate full templates blindly
- Code must be clean, readable, and commented

====================================================
GENERATION STRATEGY
====================================================
Generate files incrementally.
Start with:
1. data/OtpManager.kt
2. viewmodel/AuthState.kt
3. viewmodel/AuthViewModel.kt
Then generate UI screens one by one.
