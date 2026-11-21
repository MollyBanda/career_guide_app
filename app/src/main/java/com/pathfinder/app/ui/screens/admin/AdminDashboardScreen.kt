package com.pathfinder.app.ui.screens.admin

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pathfinder.app.data.model.*
import com.pathfinder.app.data.repository.PathwiseStudentAccount
import com.pathfinder.app.ui.components.*
import com.pathfinder.app.ui.theme.DashboardColors.Blue
import com.pathfinder.app.ui.theme.DashboardColors.Canvas
import com.pathfinder.app.ui.theme.DashboardColors.Ink
import com.pathfinder.app.ui.theme.DashboardColors.Line
import com.pathfinder.app.ui.theme.DashboardColors.Muted
import com.pathfinder.app.ui.screens.counsellor.*

private data class PendingAdminReview(val title: String, val description: String, val category: String, val details: String)

@Composable
fun AdminDashboardScreen(
    pad: PaddingValues,
    notify: (String) -> Unit,
    onProfile: () -> Unit,
    catalogItems: List<OpportunityEntity>,
    onSaveOpportunity: (OpportunityEntity) -> Unit,
    onDeleteOpportunity: (OpportunityEntity) -> Unit,
    studentAccounts: List<PathwiseStudentAccount> = emptyList(),
    onBroadcastAnnouncement: (String, String) -> Unit = { _, _ -> }
) {
    var showManager by remember { mutableStateOf(false) }
    var registryFilter by remember { mutableStateOf<String?>(null) }
    var selectedReview by remember { mutableStateOf<PendingAdminReview?>(null) }
    var selectedVital by remember { mutableStateOf<String?>(null) }
    var showAnnouncementComposer by remember { mutableStateOf(false) }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementMessage by remember { mutableStateOf("") }
    val reviews = remember {
        mutableStateListOf(
            PendingAdminReview(
                "Cybersecurity programme requirements",
                "Review a proposed entry requirement update for the cybersecurity programme.",
                "University entry update",
                "Proposed change: minimum entry score of 84. Confirm the institution, programme, and source before approving the update."
            ),
            PendingAdminReview(
                "Clean energy scholarship details",
                "Check the award description, eligibility, and application deadline.",
                "Scholarship update",
                "Review the proposed Year 12 clean energy award information. Confirm the eligibility criteria, award value, and closing date before publishing."
            )
        )
    }
    val registries = listOf(
        Triple("Careers", "Career descriptions and skills", "career"),
        Triple("Universities", "Programmes and entry requirements", "university"),
        Triple("Scholarships", "Awards and application deadlines", "scholarship"),
        Triple("Assessments", "Assessment questions and guidance", "assessment")
    )

    Column(
        Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState())
            .background(Canvas).padding(horizontal = 16.dp, vertical = 11.dp)
    ) {
        DashboardHeader("Admin Dashboard", onProfile = onProfile)
        Spacer(Modifier.height(12.dp))
        Text("System Dashboard", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(14.dp))

        SectionTitle("PLATFORM VITALS", "Current totals")
        val vitals = listOf(
            "Student accounts" to studentAccounts.size.toString(),
            "Careers available" to catalogItems.count { it.category == "Careers" }.toString(),
            "Universities available" to catalogItems.count { it.category == "Universities" && (it.id == "uni-malawi" || it.id.startsWith("uni-expanded-")) }.toString()
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 560.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    vitals.forEach { (label, value) -> VitalCard(label, value, Modifier.weight(1f)) { selectedVital = label } }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    vitals.forEach { (label, value) -> VitalCard(label, value, Modifier.fillMaxWidth()) { selectedVital = label } }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        SectionTitle("MANAGE REGISTRY", "Select a section to manage")
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 560.dp) {
                Column {
                    registries.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { (title, subtitle, icon) ->
                                RegistryCard(title, subtitle, icon, Modifier.weight(1f)) {
                                    registryFilter = registryCategory(title)
                                    showManager = true
                                }
                            }
                        }
                    }
                }
            } else {
                Column {
                    registries.forEach { (title, subtitle, icon) ->
                        RegistryCard(title, subtitle, icon, Modifier.fillMaxWidth()) {
                            registryFilter = registryCategory(title)
                            showManager = true
                        }
                    }
                }
            }
        }
        OutlinedButton(
            onClick = { registryFilter = null; showManager = true },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) { Text("Manage all catalogue items", fontSize = 15.sp) }

        Spacer(Modifier.height(14.dp))
        SectionTitle("PENDING REVIEWS", "${reviews.size} awaiting review")
        if (reviews.isEmpty()) {
            Text("No updates need review right now.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
        }
        reviews.forEach { review ->
            ReviewCard(
                review.title, review.description, review.category,
                onReview = { selectedReview = review },
                onApprove = {
                    reviews.remove(review)
                    notify("${review.title} approved.")
                }
            )
        }

        Button(
            onClick = { showAnnouncementComposer = true },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            shape = RoundedCornerShape(11.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) { Text("Broadcast System Announcement", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }

    if (showManager) {
        AdminOpportunityManager(
            catalogItems.filter { registryFilter == null || it.category == registryFilter },
            onSaveOpportunity, onDeleteOpportunity, { showManager = false }
        )
    }
    selectedVital?.let { vital ->
        val entries = when (vital) {
            "Student accounts" -> studentAccounts.map { it.fullName to it.email }
            "Careers available" -> catalogItems.filter { it.category == "Careers" }.map { it.title to it.subtitle }
            else -> catalogItems.filter { it.category == "Universities" && (it.id == "uni-malawi" || it.id.startsWith("uni-expanded-")) }.map { it.title to it.subtitle }
        }
        AlertDialog(
            onDismissRequest = { selectedVital = null },
            title = { Text("$vital (${entries.size})", fontSize = 20.sp) },
            text = {
                if (entries.isEmpty()) {
                    Text("No ${vital.lowercase()} to show yet.", color = Ink, fontSize = 16.sp)
                } else {
                    Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState())) {
                        entries.forEach { (title, subtitle) ->
                            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text(subtitle, color = Muted, fontSize = 14.sp, lineHeight = 19.sp)
                            }
                            HorizontalDivider(color = Line)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedVital = null }) { Text("Close", fontSize = 16.sp) } }
        )
    }
    selectedReview?.let { review ->
        AlertDialog(
            onDismissRequest = { selectedReview = null },
            title = { Text(review.title) },
            text = { Text(review.details, color = Muted, lineHeight = 21.sp) },
            confirmButton = {
                TextButton(onClick = {
                    reviews.remove(review)
                    selectedReview = null
                    notify("${review.title} approved.")
                }) { Text("Approve") }
            },
            dismissButton = { TextButton(onClick = { selectedReview = null }) { Text("Close") } }
        )
    }
    if (showAnnouncementComposer) {
        AlertDialog(
            onDismissRequest = { showAnnouncementComposer = false },
            title = { Text("New system announcement") },
            text = {
                Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                    Text("Write an update for users of the app.", color = Ink, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = announcementTitle,
                        onValueChange = { announcementTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Announcement title") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(7.dp))
                    OutlinedTextField(
                        value = announcementMessage,
                        onValueChange = { announcementMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Message") },
                        minLines = 3,
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (announcementTitle.isBlank() || announcementMessage.isBlank()) {
                        notify("Enter an announcement title and message.")
                    } else {
                        onBroadcastAnnouncement(announcementTitle.trim(), announcementMessage.trim())
                        announcementTitle = ""
                        announcementMessage = ""
                        showAnnouncementComposer = false
                    }
                }) { Text("Broadcast") }
            },
            dismissButton = { TextButton(onClick = { showAnnouncementComposer = false }) { Text("Cancel") } }
        )
    }
}

private fun registryCategory(title: String): String? = when (title) {
    "Universities" -> "Universities"
    "Scholarships" -> "Scholarships"
    "Assessments" -> "Assessment"
    else -> "Careers"
}

@Composable
private fun VitalCard(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, Line)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 10.dp)) {
            Text(label, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(value, color = Blue, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text("Tap to view details", color = Muted, fontSize = 12.sp)
        }
    }
}
@Composable
private fun AdminOpportunityManager(
    items: List<OpportunityEntity>,
    onSave: (OpportunityEntity) -> Unit,
    onDelete: (OpportunityEntity) -> Unit,
    onClose: () -> Unit
) {
    var editing by remember { mutableStateOf<OpportunityEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<OpportunityEntity?>(null) }

    if (editing != null) {
        OpportunityEditorDialog(
            initial = editing!!,
            onDismiss = { editing = null },
            onSave = { item -> onSave(item); editing = null }
        )
    } else if (deleteTarget != null) {
        val target = deleteTarget!!
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete opportunity?") },
            text = { Text("Delete " + target.title + " from the catalogue? Its bookmarks will also be removed.") },
            confirmButton = {
                TextButton(onClick = { onDelete(target); deleteTarget = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    } else {
        AlertDialog(
            onDismissRequest = onClose,
            title = { Text("Manage catalogue") },
            text = {
                Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                    Text("Add or update career and opportunity information stored on this device.", color = Ink, fontSize = 14.sp)
                    Button(
                        onClick = {
                            editing = OpportunityEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                title = "",
                                category = "Careers",
                                subtitle = "",
                                overview = ""
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) { Text("Add catalogue item") }
                    items.forEach { item ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(item.category, color = Ink, fontSize = 13.sp)
                            }
                            TextButton(onClick = { editing = item }) { Text("Edit") }
                            TextButton(onClick = { deleteTarget = item }) { Text("Delete", color = Color(0xFFC93748)) }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onClose) { Text("Done") } }
        )
    }
}
@Composable
private fun OpportunityEditorDialog(
    initial: OpportunityEntity,
    onDismiss: () -> Unit,
    onSave: (OpportunityEntity) -> Unit
) {
    val fields = remember(initial.id) {
        androidx.compose.runtime.mutableStateListOf(
            initial.title, initial.category, initial.subtitle, initial.overview,
            initial.qualifications, initial.courses, initial.skills, initial.pathways,
            initial.provider, initial.requirements, initial.duration, initial.deadline, initial.applicationUrl
        )
    }
    val labels = listOf(
        "Title", "Category (Careers, Universities, Scholarships, Training, Internships, Assessment)",
        "Short description", "Overview", "Qualifications", "Courses or programmes", "Skills",
        "Career pathways", "Provider", "Entry or eligibility requirements", "Duration", "Deadline", "Application link"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.title.isBlank()) "Add catalogue item" else "Edit catalogue item") },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())) {
                labels.forEachIndexed { index, label ->
                    OutlinedTextField(
                        value = fields[index],
                        onValueChange = { fields[index] = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        label = { Text(label, fontSize = 14.sp) },
                        minLines = if (index in 3..9) 2 else 1,
                        singleLine = index !in 3..9
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val category = fields[1].trim().replaceFirstChar { it.uppercase() }
                if (fields[0].isNotBlank() && fields[2].isNotBlank() && fields[3].isNotBlank() &&
                    category in setOf("Careers", "Universities", "Scholarships", "Training", "Internships", "Assessment")
                ) {
                    onSave(initial.copy(
                        title = fields[0].trim(), category = category, subtitle = fields[2].trim(), overview = fields[3].trim(),
                        qualifications = fields[4].trim(), courses = fields[5].trim(), skills = fields[6].trim(),
                        pathways = fields[7].trim(), provider = fields[8].trim(), requirements = fields[9].trim(),
                        duration = fields[10].trim(), deadline = fields[11].trim(), applicationUrl = fields[12].trim(),
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
