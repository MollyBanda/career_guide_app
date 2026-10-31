package com.pathfinder.app.ui.screens.welcome

import androidx.compose.foundation.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pathfinder.app.ui.theme.AuthColors.Blue
import com.pathfinder.app.ui.theme.AuthColors.Border
import com.pathfinder.app.ui.theme.AuthColors.Muted
import com.pathfinder.app.ui.theme.AuthColors.Navy
import com.pathfinder.app.ui.components.PrimaryButton

@Composable
private fun Brand() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(13.dp), color = Blue, shadowElevation = 5.dp) {
            Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Text("⌁", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(10.dp))
        Text("Pathfinder", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun WelcomeScreen(create: () -> Unit, signIn: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 23.dp, vertical = 18.dp)
    ) {
        Brand()
        Spacer(Modifier.height(23.dp))
        Box(
            Modifier.fillMaxWidth().height(235.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF2869EF), Color(0xFF4E50D8), Color(0xFF263F9C))),
                    RoundedCornerShape(27.dp)
                ).padding(15.dp)
        ) {
            Text("🎓", Modifier.align(Alignment.Center), fontSize = 82.sp)
        }
        Spacer(Modifier.height(22.dp))
        Surface(color = Color(0xFFE8EDFF), shape = CircleShape) {
            Text("✨  CAREER GUIDANCE MADE PERSONAL", Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                color = Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text("Find the path that feels like you.", fontSize = 36.sp, lineHeight = 42.sp,
            fontWeight = FontWeight.ExtraBold, color = Navy, letterSpacing = (-1).sp)
        Text("Discover careers, courses, scholarships, and opportunities matched to your strengths, goals, and passions.",
            Modifier.padding(top = 9.dp), color = Muted, fontSize = 15.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(20.dp))
        PrimaryButton("Create my account", create)
        Spacer(Modifier.height(9.dp))
        OutlinedButton(
            onClick = signIn,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(13.dp),
            border = BorderStroke(1.dp, Border),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
        ) { Text("I already have an account", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun BadgePill(text: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = CircleShape, color = Color.White.copy(alpha = .95f), shadowElevation = 2.dp) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = Navy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FeaturePill(text: String) {
    Surface(shape = CircleShape, color = Color.White.copy(alpha = .85f), border = BorderStroke(1.dp, Border)) {
        Text(text, Modifier.padding(horizontal = 9.dp, vertical = 7.dp), color = Color(0xFF4D5B76), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
