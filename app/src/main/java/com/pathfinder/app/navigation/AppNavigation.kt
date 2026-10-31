package com.pathfinder.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import com.pathfinder.app.data.repository.PathwiseRepository
import com.pathfinder.app.ui.screens.auth.*
import com.pathfinder.app.ui.screens.dashboard.DashboardScreen
import com.pathfinder.app.ui.screens.welcome.WelcomeScreen
import com.pathfinder.app.ui.theme.AuthColors.Page
import kotlinx.coroutines.launch

@Composable
fun PathwiseApp(notify: (String) -> Unit) {
    val context = LocalContext.current
    val repository = remember { PathwiseRepository.get(context) }
    val scope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(AppScreen.Welcome) }
    var activeEmail by remember { mutableStateOf<String?>(null) }
    var activeRole by remember { mutableStateOf("Student") }
    var profileName by remember { mutableStateOf("Maya Martinez") }
    var profileEmail by remember { mutableStateOf("maya.m@example.com") }
    val savedItems by repository.observeSavedTitles(activeEmail).collectAsState(initial = emptySet())
    val catalogItems by repository.observeCatalog().collectAsState(initial = emptyList())
    val latestAssessment by repository.observeLatestAssessment(activeEmail).collectAsState(initial = null)
    var detailTitle by remember { mutableStateOf("") }
    var detailSubtitle by remember { mutableStateOf("") }
    var detailCategory by remember { mutableStateOf("Careers") }
    var detailReturn by remember { mutableStateOf("Home") }

    LaunchedEffect(Unit) {
        try {
            val account = repository.initialize()
            if (account != null) {
                activeEmail = account.email
                activeRole = account.role
                profileName = account.fullName
                profileEmail = account.email
                screen = AppScreen.Home
            }
        } catch (error: Exception) {
            notify("Could not load saved app data. Please restart the app.")
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Page) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.linearGradient(listOf(Color(0xFFE5EAFF), Page, Color(0xFFE3F8F1)))
            )
        ) {
            when (screen) {
                AppScreen.Welcome -> WelcomeScreen(
                    create = { screen = AppScreen.SignUp },
                    signIn = { screen = AppScreen.SignIn }
                )
                AppScreen.SignIn -> SignInScreen(
                    back = { screen = AppScreen.Welcome },
                    create = { screen = AppScreen.SignUp },
                    notify = notify,
                    onLogin = { email, password, requestedRole ->
                        scope.launch {
                            try {
                                val account = repository.authenticate(email, password, requestedRole)
                                if (account == null) {
                                    notify("Email, password, or account role is incorrect.")
                                } else {
                                    activeEmail = account.email
                                    activeRole = account.role
                                    profileName = account.fullName
                                    profileEmail = account.email
                                    screen = AppScreen.Home
                                }
                            } catch (error: Exception) {
                                notify("Could not log in. Please try again.")
                            }
                        }
                    }
                )
                AppScreen.SignUp -> SignUpScreen(
                    back = { screen = AppScreen.Welcome },
                    signIn = { screen = AppScreen.SignIn },
                    notify = notify,
                    onAuthenticated = { name, email, password ->
                        scope.launch {
                            try {
                                val account = repository.register(name, email, password)
                                if (account == null) {
                                    notify("Email already registered.")
                                } else {
                                    activeEmail = account.email
                                    activeRole = account.role
                                    profileName = account.fullName
                                    profileEmail = account.email
                                    screen = AppScreen.Home
                                    notify("Account created successfully.")
                                }
                            } catch (error: Exception) {
                                notify("Could not create your account. Please try again.")
                            }
                        }
                    }
                )
                else -> DashboardScreen(
                    page = screen.name,
                    onNavigate = { destination ->
                        if (destination == "SignIn") {
                            scope.launch { repository.signOut() }
                            activeEmail = null
                            activeRole = "Student"
                        }
                        screen = AppScreen.valueOf(destination)
                    },
                    notify = notify,
                    role = activeRole,
                    profileName = profileName,
                    profileEmail = profileEmail,
                    savedTitles = savedItems,
                    catalogItems = catalogItems,
                    latestAssessment = latestAssessment,
                    onSaveOpportunity = { item ->
                        val email = activeEmail
                        if (email == null) notify("Administrator login required.")
                        else scope.launch {
                            if (!repository.saveOpportunityAsAdmin(email, item)) notify("Only an administrator can update catalogue data.")
                        }
                    },
                    onDeleteOpportunity = { item ->
                        val email = activeEmail
                        if (email == null) notify("Administrator login required.")
                        else scope.launch {
                            if (!repository.deleteOpportunityAsAdmin(email, item.id)) notify("Only an administrator can delete catalogue data.")
                        }
                    },
                    onQuizCompleted = { summary, answers ->
                        val email = activeEmail
                        if (email != null) scope.launch {
                            try { repository.saveAssessment(email, summary, answers); notify("Assessment result saved.") }
                            catch (error: Exception) { notify("Could not save your assessment result.") }
                        }
                    },
                    detailTitle = detailTitle,
                    detailSubtitle = detailSubtitle,
                    detailCategory = detailCategory,
                    detailReturn = detailReturn,
                    onOpenDetails = { title, subtitle, category, returnTo ->
                        detailTitle = title
                        detailSubtitle = subtitle
                        detailCategory = category
                        detailReturn = returnTo
                        screen = AppScreen.Details
                    },
                    onToggleSave = { title ->
                        val email = activeEmail
                        if (email == null) notify("Log in to save opportunities.")
                        else scope.launch {
                            try {
                                repository.toggleSaved(email, title)
                            } catch (error: Exception) {
                                notify("Could not update saved items. Please try again.")
                            }
                        }
                    },
                    onProfileUpdated = { name, email ->
                        val oldEmail = activeEmail
                        if (oldEmail == null) notify("Log in to update your profile.")
                        else scope.launch {
                            try {
                                val updated = repository.updateProfile(oldEmail, name, email)
                                if (updated == null) notify("That email address is already in use.")
                                else {
                                    activeEmail = updated.email
                                    profileName = updated.fullName
                                    profileEmail = updated.email
                                    notify("Profile updated successfully.")
                                }
                            } catch (error: Exception) {
                                notify("Could not update your profile. Please try again.")
                            }
                        }
                    }
                )
            }
        }
    }
}
