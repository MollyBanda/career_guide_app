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
import java.util.UUID

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
        Triple("University Management", "Universities and campus profiles", "university"),
        Triple("University Programme Management", "Academic awards, subjects, and durations", "degree"),
        Triple("Career Management", "Roles, pathways, and skills", "career"),
        Triple("Scholarship Management", "Funding opportunities and deadlines", "scholarship"),
        Triple("Internship Management", "Workplace placements and gainful experience", "internship"),
        Triple("Training Management", "Skills bootcamps and online courses", "training"),
        Triple("User Management", "Student and user accounts", "person")
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(pad)
            .verticalScroll(rememberScrollState())
            .background(Canvas)
            .padding(horizontal = 16.dp, vertical = 11.dp)
    ) {
        DashboardHeader("Admin Dashboard", onProfile = onProfile)
        Spacer(Modifier.height(12.dp))
        Text("System Dashboard", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(14.dp))

        SectionTitle("PLATFORM VITALS", "Current totals")
        val vitals = listOf(
            "Student accounts" to studentAccounts.size.toString(),
            "Careers available" to catalogItems.count { it.category == "Careers" }.toString(),
            "Universities available" to catalogItems.count { it.category == "Universities" && (it.id == "uni-malawi" || it.id.startsWith("uni-expanded-")) }.toString(),
            "Scholarships available" to catalogItems.count { it.category == "Scholarships" }.toString()
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 560.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    vitals.forEach { (label, value) -> VitalCard(label, value, Modifier.weight(1f)) { selectedVital = label } }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    vitals.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            row.forEach { (label, value) ->
                                VitalCard(label, value, Modifier.weight(1f)) { selectedVital = label }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        SectionTitle("MANAGE REGISTRY", "Select a section to manage")
        Column {
            registries.forEach { (title, subtitle, icon) ->
                RegistryCard(title, subtitle, icon, Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    if (title == "User Management") {
                        selectedVital = "Student accounts"
                    } else {
                        registryFilter = registryCategory(title)
                        showManager = true
                    }
                }
            }
        }

        OutlinedButton(
            onClick = { registryFilter = null; showManager = true },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            shape = RoundedCornerShape(10.dp)
        ) { Text("Manage All Catalogue Items", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }

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
        ) { Text("Broadcast System Announcement", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
    }

    if (showManager) {
        AdminOpportunityManager(
            items = catalogItems.filter { registryFilter == null || it.category == registryFilter },
            categoryFilter = registryFilter,
            onSave = onSaveOpportunity,
            onDelete = onDeleteOpportunity,
            onClose = { showManager = false }
        )
    }

    selectedVital?.let { vital ->
        val entries = when (vital) {
            "Student accounts" -> studentAccounts.map { it.fullName to it.email }
            "Careers available" -> catalogItems.filter { it.category == "Careers" }.map { it.title to it.subtitle }
            "Scholarships available" -> catalogItems.filter { it.category == "Scholarships" }.map { it.title to it.subtitle }
            else -> catalogItems.filter { it.category == "Universities" && (it.id == "uni-malawi" || it.id.startsWith("uni-expanded-")) }.map { it.title to it.subtitle }
        }
        AlertDialog(
            onDismissRequest = { selectedVital = null },
            title = { Text("$vital (${entries.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                if (entries.isEmpty()) {
                    Text("No ${vital.lowercase()} to show yet.", color = Ink, fontSize = 14.sp)
                } else {
                    Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState())) {
                        entries.forEach { (title, subtitle) ->
                            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(subtitle, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
                            }
                            HorizontalDivider(color = Line)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedVital = null }) { Text("Close", fontSize = 14.sp) } }
        )
    }

    selectedReview?.let { review ->
        AlertDialog(
            onDismissRequest = { selectedReview = null },
            title = { Text(review.title) },
            text = { Text(review.details, color = Muted, lineHeight = 20.sp) },
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
            title = { Text("New System Announcement") },
            text = {
                Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                    Text("Write an update for all app users.", color = Ink, fontSize = 13.sp)
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

private fun registryCategory(title: String): String? = when {
    title.contains("University Programme", ignoreCase = true) -> "Universities"
    title.contains("University", ignoreCase = true) -> "Universities"
    title.contains("Scholarship", ignoreCase = true) -> "Scholarships"
    title.contains("Internship", ignoreCase = true) -> "Internships"
    title.contains("Training", ignoreCase = true) -> "Training"
    title.contains("Assessment", ignoreCase = true) -> "Assessment"
    else -> "Careers"
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(Modifier.padding(bottom = 6.dp)) {
        Text(title, color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .5.sp)
        Text(subtitle, color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun VitalCard(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(11.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, Line)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 10.dp)) {
            Text(label, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(value, color = Blue, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("Tap to view details", color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun RegistryCard(title: String, subtitle: String, icon: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Line)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FeatureIcon(icon)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Muted, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ReviewCard(title: String, description: String, category: String, onReview: () -> Unit, onApprove: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Line)
    ) {
        Column(Modifier.padding(12.dp)) {
            Label(category.uppercase(), Color(0xFFE8EDFF), Blue)
            Text(title, modifier = Modifier.padding(top = 6.dp), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(description, color = Muted, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onReview, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp)) {
                    Text("Review Details", fontSize = 11.sp)
                }
                Button(onClick = onApprove, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                    Text("Approve Update", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AdminOpportunityManager(
    items: List<OpportunityEntity>,
    categoryFilter: String?,
    onSave: (OpportunityEntity) -> Unit,
    onDelete: (OpportunityEntity) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<OpportunityEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<OpportunityEntity?>(null) }

    val filteredItems = remember(items, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) items
        else items.filter {
            it.title.contains(q, ignoreCase = true) ||
                    it.subtitle.contains(q, ignoreCase = true) ||
                    it.category.contains(q, ignoreCase = true)
        }
    }

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
            title = { Text("Delete item?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${target.title}' from the catalogue? This will also remove any user bookmarks for this item.") },
            confirmButton = {
                TextButton(onClick = { onDelete(target); deleteTarget = null }) { Text("Delete", color = Color(0xFFC93748)) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    } else {
        AlertDialog(
            onDismissRequest = onClose,
            title = { Text(if (categoryFilter != null) "Manage $categoryFilter" else "Manage Catalogue", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        placeholder = { Text("Search items...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Muted) },
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            editing = OpportunityEntity(
                                id = UUID.randomUUID().toString(),
                                title = "",
                                category = categoryFilter ?: "Careers",
                                subtitle = "",
                                overview = ""
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue)
                    ) { Text("Add New Record", fontSize = 13.sp, fontWeight = FontWeight.Bold) }

                    Spacer(Modifier.height(6.dp))

                    if (filteredItems.isEmpty()) {
                        Text("No matching items found.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        filteredItems.forEach { item ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Line)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(item.title, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("${item.category} · ${item.subtitle}", color = Muted, fontSize = 10.sp)
                                    }
                                    TextButton(onClick = { editing = item }, contentPadding = PaddingValues(horizontal = 6.dp)) { Text("Edit", fontSize = 12.sp) }
                                    TextButton(onClick = { deleteTarget = item }, contentPadding = PaddingValues(horizontal = 6.dp)) { Text("Delete", color = Color(0xFFC93748), fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onClose) { Text("Done", fontSize = 14.sp) } }
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
        mutableStateListOf(
            initial.title, initial.category, initial.subtitle, initial.overview,
            initial.qualifications, initial.courses, initial.skills, initial.pathways,
            initial.provider, initial.requirements, initial.duration, initial.deadline, initial.applicationUrl
        )
    }
    val labels = listOf(
        "Title", "Category (Careers, Universities, Scholarships, Training, Internships, Assessment)",
        "Short description / subtitle", "Overview", "Qualifications", "Courses / University Programmes", "Skills",
        "Career pathways", "Provider / Institution", "Entry or eligibility requirements", "Duration", "Deadline", "Official website / application link"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.title.isBlank()) "Add New Record" else "Edit Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())) {
                labels.forEachIndexed { index, label ->
                    OutlinedTextField(
                        value = fields[index],
                        onValueChange = { fields[index] = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        label = { Text(label, fontSize = 12.sp) },
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
                        title = fields[0].trim(),
                        category = category,
                        subtitle = fields[2].trim(),
                        overview = fields[3].trim(),
                        qualifications = fields[4].trim(),
                        courses = fields[5].trim(),
                        skills = fields[6].trim(),
                        pathways = fields[7].trim(),
                        provider = fields[8].trim(),
                        requirements = fields[9].trim(),
                        duration = fields[10].trim(),
                        deadline = fields[11].trim(),
                        applicationUrl = fields[12].trim(),
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            }) { Text("Save Record", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontSize = 14.sp) } }
    )
}
