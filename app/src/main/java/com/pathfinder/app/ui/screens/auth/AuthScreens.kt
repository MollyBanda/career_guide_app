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

@Composable
private fun Header(back: () -> Unit, title: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = back, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(5.dp)); Text("Back", color = Muted, fontSize = 14.sp)
        }
        Text(title, color = Muted, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = .45.sp)
    }
}
@Composable
fun SignUpScreen(back: () -> Unit, signIn: () -> Unit, notify: (String) -> Unit, onAuthenticated: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf("College / Uni") }
    var terms by remember { mutableStateOf(false) }
    var alerts by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 23.dp, vertical = 14.dp)) {
        Header(back, "STEP 1 OF 2 · BASIC INFO")
        Text("Create your account ✨", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Text("Join 50k+ students and professionals finding their dream career path with clarity and joy.",
            Modifier.padding(top = 5.dp, bottom = 12.dp), color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF2F3FF), border = BorderStroke(1.dp, Border)) {
            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("◎", color = Blue, fontSize = 22.sp); Spacer(Modifier.width(9.dp))
                Text("Personalized matches & $15M+ in scholarships await", color = Navy, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
        Spacer(Modifier.height(9.dp))
        FormField("Full Name", name, { name = it }, "e.g. Maya Martinez", Icons.Default.Person)
        FormField("Email Address", email, { email = it }, "e.g. maya.m@example.com", Icons.Default.Email, KeyboardType.Email)
        FormField("Password", password, { password = it }, "At least 6 characters", Icons.Default.Lock,
            KeyboardType.Password, true, passwordVisible) { passwordVisible = !passwordVisible }
        FormField("Confirm Password", confirmPassword, { confirmPassword = it }, "Re-enter your password", Icons.Default.Lock,
            KeyboardType.Password, true, passwordVisible) { passwordVisible = !passwordVisible }
        val strength = when {
            password.isEmpty() -> "ADD A PASSWORD"
            password.length < 6 -> "WEAK PASSWORD"
            password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> "STRONG PASSWORD"
            else -> "GOOD PASSWORD"
        }
        Text("●  $strength", Modifier.align(Alignment.End), color = if (strength == "WEAK PASSWORD") Color(0xFFC85D52) else Color(0xFF138E70),
            fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        color = if (selected) Color(0xFFF0F3FF) else Color.White,
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
        ConsentRow(terms, { terms = it }, "I agree to the Terms of Service & Privacy Policy.", required = true)
        ConsentRow(alerts, { alerts = it }, "Send me personalized scholarship alerts & curated career insights.")
        Spacer(Modifier.height(5.dp))
        PrimaryButton("Create Account") {
            when {
                name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> notify("Please fill all fields.")
                !email.contains("@") -> notify("Enter a valid email address.")
                password.length < 6 -> notify("Password must be at least 6 characters.")
                password != confirmPassword -> notify("Passwords do not match.")
                !terms -> notify("Please agree to the Terms of Service and Privacy Policy.")
                else -> { notify("Account created successfully."); onAuthenticated(name.trim(), email.trim(), password) }
            }
        }
        SwitchLine("Already have an account?", "Log in", signIn)
        SecureLine()
    }
}

@Composable
fun SignInScreen(back: () -> Unit, create: () -> Unit, notify: (String) -> Unit, onLogin: (String, String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var role by remember { mutableStateOf("Student") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 23.dp, vertical = 14.dp)) {
        Header(back, "PATHFINDER ID  ✨")
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            listOf("Student","Counsellor","Admin").forEach { option ->
                val selected=role==option
                Surface(Modifier.weight(1f).height(38.dp).clickable { role=option },shape=RoundedCornerShape(10.dp),color=if(selected) Color(0xFFE8EDFF) else Color.White,border=BorderStroke(1.dp,if(selected) Blue else Border)) {
                    Box(contentAlignment=Alignment.Center){Text(option,color=if(selected) Blue else Muted,fontSize=11.sp,fontWeight=FontWeight.SemiBold)}
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Welcome back! 👋", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Text("Log in to pick up where you left off on your career discovery path.",
            Modifier.padding(top = 5.dp, bottom = 12.dp), color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF2F3FF), border = BorderStroke(1.dp, Border)) {
            Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("◉", color = Blue, fontSize = 19.sp); Spacer(Modifier.width(9.dp))
                Column {
                    Text("Data Science Explorer", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Last active: 2 days ago · 80% Match", color = Muted, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f)); Text("♧", color = Muted, fontSize = 17.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        FormField("Email address", email, { email = it }, "alex.chen@example.com", Icons.Default.Email, KeyboardType.Email)
        FormField("Password", password, { password = it }, "Enter your password", Icons.Default.Lock,
            KeyboardType.Password, true, passwordVisible) { passwordVisible = !passwordVisible }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(rememberMe, { rememberMe = it }, modifier = Modifier.size(30.dp),
                    colors = CheckboxDefaults.colors(checkedColor = Blue))
                Text("Remember me", color = Muted, fontSize = 12.sp)
            }
            TextButton(onClick = { notify("If this email is registered, password reset instructions will be sent.") },
                contentPadding = PaddingValues(0.dp)) {
                Text("Forgot password?", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        PrimaryButton("Login") {
            when {
                !email.contains("@") || password.isBlank() -> notify("Enter a valid email address and password.")
                else -> onLogin(email.trim(), password, role)
            }
        }
        SwitchLine("Don't have an account?", "Create Account", create)
        SecureLine()
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
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
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
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)) {
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
private fun SecureLine() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = Muted, modifier = Modifier.size(11.dp))
        Spacer(Modifier.width(4.dp)); Text("256-bit SSL encrypted & secure", color = Muted, fontSize = 11.sp)
    }
}
