package com.pathfinder.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.os.Handler
import android.os.Looper
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
    var isWorking by remember { mutableStateOf(false) }
    var activeUid by remember { mutableStateOf<String?>(null) }
    var activeRole by remember { mutableStateOf("Student") }
    var profileName by remember { mutableStateOf("Maya Martinez") }
    var profileEmail by remember { mutableStateOf("maya.m@example.com") }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val savedItemsFlow = remember(activeUid) {
        repository.observeSavedTitles(activeUid) {
            mainHandler.post { notify("Could not load your saved items. Check your connection and account permissions.") }
        }
    }
    val savedItems by savedItemsFlow.collectAsState(initial = emptySet())
    val catalogItems by repository.observeCatalog().collectAsState(initial = emptyList())
    val assessmentFlow = remember(activeUid) {
        repository.observeLatestAssessment(activeUid) {
            mainHandler.post { notify("Could not load your assessment history. Check your connection and account permissions.") }
        }
    }
    val latestAssessment by assessmentFlow.collectAsState(initial = null)
    var detailTitle by remember { mutableStateOf("") }
    var detailSubtitle by remember { mutableStateOf("") }
    var detailCategory by remember { mutableStateOf("Careers") }
    var detailReturn by remember { mutableStateOf("Home") }

    LaunchedEffect(Unit) {
        try {
            val account = repository.initialize()
            if (account != null) {
                activeUid = account.uid.takeIf { account.cloudSynced }
                activeRole = account.role
                profileName = account.fullName
                profileEmail = account.email
                screen = AppScreen.Home
                if (!account.cloudSynced) notify("Signed in with this device's cached profile. Firebase cloud sync is still pending.")
            }
        } catch (error: Exception) {
            notify(repository.explainAccountError(error))
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
                    onForgotPassword = { email ->
                        scope.launch {
                            isWorking = true
                            try {
                                repository.sendPasswordResetEmail(email)
                                notify("If that email has an account, Firebase sent password-reset instructions. Check your inbox and spam folder.")
                            } catch (error: Exception) {
                                notify(repository.explainAccountError(error))
                            } finally {
                                isWorking = false
                            }
                        }
                    },
                    onLogin = { email, password, requestedRole ->
                        scope.launch {
                            isWorking = true
                            try {
                                val account = repository.authenticate(email, password, requestedRole)
                                if (account == null) {
                                    notify("Email, password, or selected account type is incorrect.")
                                } else {
                                    activeUid = account.uid.takeIf { account.cloudSynced }
                                    activeRole = account.role
                                    profileName = account.fullName
                                    profileEmail = account.email
                                    screen = AppScreen.Home
                                    if (!account.cloudSynced) notify("Signed in with this device's cached profile. Firebase cloud sync is still pending.")
                                }
                            } catch (error: Exception) {
                                notify(repository.explainAccountError(error))
                            } finally {
                                isWorking = false
                            }
                        }
                    }
                )
                AppScreen.SignUp -> SignUpScreen(
                    back = { screen = AppScreen.Welcome },
                    signIn = { screen = AppScreen.SignIn },
                    notify = notify,
                    onAuthenticated = { name, email, password, role ->
                        scope.launch {
                            isWorking = true
                            try {
                                val account = repository.register(name, email, password, role)
                                if (account == null) {
                                    notify("Email already registered.")
                                } else {
                                    activeUid = account.uid.takeIf { account.cloudSynced }
                                    activeRole = account.role
                                    profileName = account.fullName
                                    profileEmail = account.email
                                    screen = AppScreen.Home
                                    notify(if (account.cloudSynced) "Account created successfully." else "Account created. Cloud profile sync is pending; you can continue using the app on this device.")
                                }
                            } catch (error: Exception) {
                                val localAccount = runCatching {
                                    repository.registerLocally(name, email, password, role)
                                }.getOrNull()
                                if (localAccount != null) {
                                    activeUid = null
                                    activeRole = localAccount.role
                                    profileName = localAccount.fullName
                                    profileEmail = localAccount.email
                                    screen = AppScreen.Home
                                    notify("Account created on this device. Firebase setup is incomplete, so cloud sync is pending.")
                                } else {
                                    notify(repository.explainAccountError(error))
                                }
                            } finally {
                                isWorking = false
                            }
                        }
                    }
                )
                else -> DashboardScreen(
                    page = screen.name,
                    onNavigate = { destination ->
                        if (destination == "SignIn") {
                            scope.launch { repository.signOut() }
                            activeUid = null
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
                        val uid = activeUid
                        if (uid == null) notify("Administrator login required.")
                        else scope.launch {
                            if (!repository.saveOpportunityAsAdmin(uid, item)) notify("Only an administrator can update catalogue data.")
                        }
                    },
                    onDeleteOpportunity = { item ->
                        val uid = activeUid
                        if (uid == null) notify("Administrator login required.")
                        else scope.launch {
                            if (!repository.deleteOpportunityAsAdmin(uid, item.id)) notify("Only an administrator can delete catalogue data.")
                        }
                    },
                    onQuizCompleted = { summary, answers ->
                        val uid = activeUid
                        if (uid != null) scope.launch {
                            isWorking = true
                            try { repository.saveAssessment(uid, summary, answers); notify("Assessment and recommendations saved to your account.") }
                            catch (error: Exception) { notify(repository.explainAccountError(error)) }
                            finally { isWorking = false }
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
                        val uid = activeUid
                        if (uid == null) notify("Log in to save opportunities.")
                        else scope.launch {
                            isWorking = true
                            try {
                                repository.toggleSaved(uid, title)
                                notify("Saved items updated.")
                            } catch (error: Exception) {
                                notify(repository.explainAccountError(error))
                            } finally {
                                isWorking = false
                            }
                        }
                    },
                    onProfileUpdated = { name, email ->
                        val uid = activeUid
                        if (uid == null) notify("Log in to update your profile.")
                        else scope.launch {
                            isWorking = true
                            try {
                                val updated = repository.updateProfile(uid, name, email)
                                profileName = updated.fullName
                                profileEmail = updated.email
                                notify("Profile updated in Firestore.")
                            } catch (error: Exception) {
                                notify(repository.explainAccountError(error))
                            } finally {
                                isWorking = false
                            }
                        }
                    }
                )
            }
            if (isWorking) {
                Surface(
                    modifier = Modifier.fillMaxSize().clickable(onClick = {}),
                    color = Color.Black.copy(alpha = 0.12f)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Surface(color = Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp), shadowElevation = 6.dp) {
                            Row(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(12.dp))
                                androidx.compose.material3.Text("Please wait…")
                            }
                        }
                    }
                }
            }
        }
    }
}
