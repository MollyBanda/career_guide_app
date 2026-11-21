package com.pathfinder.app.ui.screens.counsellor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pathfinder.app.ui.components.DashboardHeader
import com.pathfinder.app.ui.theme.DashboardColors.Blue
import com.pathfinder.app.ui.theme.DashboardColors.Canvas
import com.pathfinder.app.ui.theme.DashboardColors.Ink
import com.pathfinder.app.ui.theme.DashboardColors.Line
import com.pathfinder.app.ui.theme.DashboardColors.Muted

private data class CounsellorContact(val name: String, val focus: String, val availability: String)

@Composable
fun CounsellorDashboardScreen(pad: PaddingValues, notify: (String) -> Unit, onProfile: () -> Unit) {
    var openPanel by remember { mutableStateOf<Pair<String, String>?>(null) }
    val counsellors = listOf(
        CounsellorContact("Maria Banda", "Career and subject guidance", "Available now"),
        CounsellorContact("Elijah Nthenda", "University and scholarship guidance", "Available today")
    )

    Column(
        Modifier.fillMaxSize().padding(pad).background(Canvas)
            .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 11.dp)
    ) {
        DashboardHeader("Counsellor Dashboard", onProfile = onProfile)
        Spacer(Modifier.height(14.dp))

        SectionTitle("AVAILABLE COUNSELLORS", "${counsellors.size} counsellors")
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 560.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    counsellors.forEach { counsellor ->
                        CounsellorCard(
                            counsellor,
                            Modifier.weight(1f),
                            onClick = { openPanel = counsellor.name to "${counsellor.focus}. ${counsellor.availability}. Contact ${counsellor.name} to arrange a guidance session." }
                        )
                    }
                }
            } else {
                Column {
                    counsellors.forEach { counsellor ->
                        CounsellorCard(
                            counsellor,
                            Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            onClick = { openPanel = counsellor.name to "${counsellor.focus}. ${counsellor.availability}. Contact ${counsellor.name} to arrange a guidance session." }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(7.dp))
        SectionTitle("NEXT SESSION", "10:15 AM")
        ActionCard(
            "Marcus Chen (Yr 12)", "Engineering vs Clean Energy", "10:15 AM · 30 min", "Join"
        ) {
            openPanel = "Session with Marcus Chen" to "Engineering vs Clean Energy\nToday at 10:15 AM · 30 minutes\n\nReview Marcus's interests and compare engineering and clean energy study pathways during the session."
        }

        Spacer(Modifier.height(8.dp))
        SectionTitle("ATTENTION NEEDED", "2 students")
        ActionCard(
            "Sarah Jenkins", "Scholarship review · 3 days left", "Scholarship application", "View"
        ) {
            openPanel = "Sarah Jenkins · Application results" to "Scholarship application review\n\nDeadline: in 3 days\nNext step: review eligibility, required documents, and application progress with Sarah."
        }
        ActionCard(
            "Liam Zhao", "ATAR prerequisite mismatch", "Course planning", "Bridge"
        ) {
            openPanel = "Liam Zhao · Bridging plan" to "Course planning support\n\nA prerequisite mismatch was flagged. Review eligible bridging or foundation subjects, confirm entry requirements with the institution, and agree on the next step with Liam."
        }

        Spacer(Modifier.height(8.dp))
        SectionTitle("TODAY'S SCHEDULE", "2 sessions")
        ScheduleRow("10:15 AM", "Marcus Chen", "Course Mapping") {
            openPanel = "Marcus Chen · Course Mapping" to "10:15 AM · 30 minutes\nEngineering versus clean energy study pathways."
        }
        HorizontalDivider(color = Line)
        ScheduleRow("11:30 AM", "Priya Patel", "Career Fit Review") {
            openPanel = "Priya Patel · Career Fit Review" to "11:30 AM · Review Priya's interests and discuss career options that fit her goals."
        }
    }

    openPanel?.let { (title, details) ->
        AlertDialog(
            onDismissRequest = { openPanel = null },
            title = { Text(title) },
            text = { Text(details, color = Muted, lineHeight = 21.sp) },
            confirmButton = {
                TextButton(onClick = {
                    notify("Opened $title.")
                    openPanel = null
                }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { openPanel = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun CounsellorCard(counsellor: CounsellorContact, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Line)
    ) {
        Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Color(0xFFE4F6F1), shape = CircleShape) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF008C82), modifier = Modifier.padding(9.dp).size(20.dp))
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(counsellor.name, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(counsellor.focus, color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
                Text(counsellor.availability, color = Color(0xFF008C82), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 7.dp)) {
                Text("View", color = Blue, fontSize = 10.sp)
            }
        }
    }
}
