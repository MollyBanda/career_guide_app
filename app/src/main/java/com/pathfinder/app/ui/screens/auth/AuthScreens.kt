package com.pathfinder.app.ui.screens.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pathfinder.app.ui.theme.AuthColors.Blue
import com.pathfinder.app.ui.theme.AuthColors.Border
import com.pathfinder.app.ui.theme.AuthColors.Muted
import com.pathfinder.app.ui.theme.AuthColors.Navy
import com.pathfinder.app.ui.components.PrimaryButton
import com.pathfinder.app.data.validation.PathwiseInputValidation
import kotlinx.coroutines.delay

@Composable
private fun Header(back: () -> Unit, title: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = back, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(5.dp)); Text("Back", color = Muted, fontSize = 14.sp)
        }
        if (title.isNotBlank()) {
            Text(title, color = Muted, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = .45.sp)
        }
    }
}
@Composable
fun SignUpScreen(back: () -> Unit, signIn: () -> Unit, notify: (String) -> Unit, onAuthenticated: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf("") }
    var accountRole by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf(false) }
    var alerts by remember { mutableStateOf(true) }
    val passwordIsStrong = password.length >= 8 &&
        password.any { it.isUpperCase() } && password.any { it.isLowerCase() } &&
        password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() }
    val passwordHint = when {
        password.isBlank() -> null
        password.length < 6 -> "WEAK PASSWORD"
        passwordIsStrong -> "STRONG PASSWORD"
        else -> null
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 23.dp, vertical = 14.dp)) {
        Header(back, "")
        Text("Create your account", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Text("Join 50k+ students and professionals finding their dream career path with clarity and joy.",
            Modifier.padding(top = 5.dp, bottom = 12.dp), color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
        FormField("Full Name", name, { name = it }, "e.g. Maya Martinez", Icons.Default.Person)
        FormField("Email Address", email, { email = it }, "e.g. maya.m@example.com", Icons.Default.Email, KeyboardType.Email)
        FormField("Password", password, { password = it }, "At least 6 characters", Icons.Default.Lock,
            KeyboardType.Password, true, passwordVisible) { passwordVisible = !passwordVisible }
        passwordHint?.let { hint ->
            Text(
                "●  $hint", Modifier.align(Alignment.End),
                color = if (hint == "STRONG PASSWORD") Color(0xFF138E70) else Color(0xFFC85D52),
                fontSize = 11.sp, fontWeight = FontWeight.Bold
            )
        }
        FormField("Confirm Password", confirmPassword, { confirmPassword = it }, "Re-enter your password", Icons.Default.Lock,
            KeyboardType.Password, true, passwordVisible) { passwordVisible = !passwordVisible }
        Text("Account type", Modifier.padding(top = 8.dp, bottom = 6.dp), color = Navy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            PathwiseInputValidation.registrationRoles.forEach { option ->
                val selected = accountRole == option
                Surface(
                    modifier = Modifier.weight(1f).height(38.dp).clickable { accountRole = option },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (selected) Blue else Border)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(option, color = if (selected) Blue else Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        if (accountRole == "Student") {
            Text("Where are you right now?  Helps tailor paths", Modifier.padding(top = 10.dp, bottom = 6.dp),
                color = Navy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            val levels = listOf("High School", "College / Uni", "Career Switcher", "Exploring")
            levels.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    row.forEach { option ->
                        val selected = level == option
                        Surface(
                            modifier = Modifier.weight(1f).height(38.dp).clickable { level = option },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (selected) Blue else Border)
                        ) {
                            Row(Modifier.fillMaxSize().padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(option, color = if (selected) Blue else Muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                if (selected) Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Blue, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
        ConsentRow(terms, { terms = it }, "I agree to the Terms of Service & Privacy Policy.", required = true)
        ConsentRow(alerts, { alerts = it }, "Send me personalized scholarship alerts & curated career insights.")
        Spacer(Modifier.height(5.dp))
        PrimaryButton("Create Account") {
            val validationError = PathwiseInputValidation.registrationError(
                name, email, password, confirmPassword, terms
            )
            if (validationError != null) {
                notify(validationError)
            } else if (accountRole.isBlank()) {
                notify("Choose an account type.")
            } else if (accountRole == "Student" && level.isBlank()) {
                notify("Choose where you are right now.")
            } else {
                onAuthenticated(name.trim(), email.trim(), password, accountRole, if (accountRole == "Student") level else "Exploring")
            }
        }
        SwitchLine("Already have an account?", "Log in", signIn)
    }
}

@Composable
fun SignInScreen(
    back: () -> Unit,
    create: () -> Unit,
    notify: (String) -> Unit,
    onLogin: (String, String, String) -> Unit,
    onForgotPassword: (String) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 23.dp, vertical = 14.dp)) {
        Header(back, "PATHFINDER ID")
        Text("Welcome back!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Text("Log in to pick up where you left off on your career discovery path.",
            Modifier.padding(top = 5.dp, bottom = 12.dp), color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
        Spacer(Modifier.height(8.dp))
        FormField("Email address", email, { email = it }, "alex.chen@example.com", Icons.Default.Email, KeyboardType.Email)
        FormField("Password", password, { password = it }, "Enter your password", Icons.Default.Lock,
            KeyboardType.Password, true, passwordVisible) { passwordVisible = !passwordVisible }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(rememberMe, { rememberMe = it }, modifier = Modifier.size(30.dp),
                    colors = CheckboxDefaults.colors(checkedColor = Blue))
                Text("Remember me", color = Muted, fontSize = 12.sp)
            }
            TextButton(onClick = {
                if (email.contains("@")) onForgotPassword(email.trim())
                else notify("Enter the email address for your account first.")
            },
                contentPadding = PaddingValues(0.dp)) {
                Text("Forgot password?", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        PrimaryButton("Login") {
            when {
                PathwiseInputValidation.loginError(email, password) != null ->
                    notify(PathwiseInputValidation.loginError(email, password)!!)
                else -> onLogin(email.trim(), password, "")
            }
        }
        SwitchLine("Don't have an account?", "Create Account", create)
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboard: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    togglePassword: (() -> Unit)? = null
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 7.dp)) {
        Text(label, color = Navy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            placeholder = { Text(placeholder, color = Color(0xFF98A2B4), fontSize = 12.sp) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Muted, modifier = Modifier.size(17.dp)) },
            trailingIcon = if (isPassword) ({
                IconButton(onClick = { togglePassword?.invoke() }) {
                    Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Muted, modifier = Modifier.size(17.dp))
                }
            }) else null,
            singleLine = true,
            shape = RoundedCornerShape(11.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Border,
                focusedBorderColor = Blue,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = Navy)
        )
    }
}

@Composable
private fun ConsentRow(checked: Boolean, onChecked: (Boolean) -> Unit, text: String, required: Boolean = false) {
    Row(Modifier.fillMaxWidth().clickable { onChecked(!checked) }.padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked, onChecked, modifier = Modifier.size(28.dp), colors = CheckboxDefaults.colors(checkedColor = Blue))
        Text(text, color = Navy, fontSize = 11.sp, lineHeight = 15.sp)
    }
}

@Composable
private fun DividerText(label: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = Border)
        Text("  $label  ", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        HorizontalDivider(Modifier.weight(1f), color = Border)
    }
}

@Composable
private fun ProviderButton(mark: String, name: String, modifier: Modifier = Modifier, action: () -> Unit) {
    OutlinedButton(onClick = action, modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Text(mark, color = if (name == "Google") Color(0xFF4285F4) else Navy, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(7.dp)); Text(name, color = Navy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SwitchLine(prefix: String, actionText: String, action: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 11.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(prefix, color = Muted, fontSize = 12.sp)
        TextButton(onClick = action, contentPadding = PaddingValues(start = 4.dp, end = 0.dp)) {
            Text(actionText, color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VerifyEmailScreen(
    email: String,
    notify: (String) -> Unit,
    onCheckVerified: () -> Unit,
    onResendEmail: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var isResending by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    var cooldownSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000L)
            cooldownSeconds--
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 23.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(onBackToLogin, "EMAIL VERIFICATION")
        Spacer(Modifier.height(20.dp))
        Surface(
            color = Color(0xFFE8EDFF),
            shape = CircleShape,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MarkEmailUnread,
                    contentDescription = "Unread Email",
                    tint = Blue,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "Verify Your Email Address",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Navy,
            textAlign = TextAlign.Center
        )
        Text(
            text = "We have sent a verification link to your email address. Please open your inbox and click the link to verify your email before continuing to Pathwise",
            modifier = Modifier.padding(top = 10.dp, bottom = 14.dp),
            color = Muted,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )
        if (email.isNotBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Border),
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
            ) {
                Text(
                    text = email,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = Blue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Surface(
            color = Color(0xFFFFF8E7),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0xFFFFE0B2)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "If you don't see the email, check your spam or junk folder.",
                    color = Color(0xFFBF360C),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        PrimaryButton(if (isChecking) "Checking Status..." else "I Have Verified My Email") {
            if (!isChecking) {
                isChecking = true
                onCheckVerified()
                isChecking = false
            }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                if (!isResending && cooldownSeconds == 0) {
                    isResending = true
                    onResendEmail()
                    isResending = false
                    cooldownSeconds = 30
                }
            },
            enabled = !isResending && cooldownSeconds == 0,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(11.dp),
            border = BorderStroke(1.dp, if (cooldownSeconds > 0) Border else Blue),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue)
        ) {
            Text(
                text = when {
                    isResending -> "Sending Email..."
                    cooldownSeconds > 0 -> "Resend Email in ${cooldownSeconds}s"
                    else -> "Resend Verification Email"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Back to Login", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
