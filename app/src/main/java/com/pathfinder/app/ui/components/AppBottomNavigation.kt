package com.pathfinder.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.pathfinder.app.ui.theme.DashboardColors.Blue
import com.pathfinder.app.ui.theme.DashboardColors.Muted

@Composable
fun AppBottomNavigation(page: String, onNavigate: (String) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(60.dp), verticalAlignment = Alignment.CenterVertically) {
            BottomTab(Icons.Filled.Home, "Home", page == "Home", { onNavigate("Home") }, Modifier.weight(1f))
            BottomTab(Icons.Filled.Search, "Explore", page == "Explore", { onNavigate("Explore") }, Modifier.weight(1f))
            BottomTab(Icons.Filled.BookmarkBorder, "Saved", page == "Saved", { onNavigate("Saved") }, Modifier.weight(1f))
            BottomTab(Icons.Filled.Person, "Profile", page == "Profile", { onNavigate("Profile") }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BottomTab(icon: ImageVector, label: String, active: Boolean, click: () -> Unit, modifier: Modifier) {
    Column(modifier.clickable(onClick = click).padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = if (active) Blue else Muted, modifier = Modifier.size(21.dp))
        Text(label, color = if (active) Blue else Muted, fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
    }
}
