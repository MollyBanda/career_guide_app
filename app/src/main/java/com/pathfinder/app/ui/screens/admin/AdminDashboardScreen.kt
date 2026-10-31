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
import com.pathfinder.app.ui.components.*
import com.pathfinder.app.ui.theme.DashboardColors.*

import com.pathfinder.app.ui.screens.counsellor.*

@Composable fun AdminDashboardScreen(pad:PaddingValues,notify:(String)->Unit,onProfile:()->Unit,catalogItems:List<OpportunityEntity>,onSaveOpportunity:(OpportunityEntity)->Unit,onDeleteOpportunity:(OpportunityEntity)->Unit){
  val showManager=remember { mutableStateOf(false) }

     Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).background(Canvas).padding(horizontal=15.dp,
        vertical=10.dp)){

          DashboardHeader("Admin Dashboard",onProfile=onProfile)
        Spacer(Modifier.height(10.dp))
          Row(verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f)){
                Text("ADMIN CONSOLE",color=Blue,fontSize=10.sp,fontWeight=FontWeight.Bold)
                Text("System Dashboard",color=Ink,fontSize=18.sp,fontWeight=FontWeight.ExtraBold)
            }
            Label("● All Operational",Color(0xFFD8F7F1),Color(0xFF007C78))
        }

          Spacer(Modifier.height(12.dp))
        SectionTitle("PLATFORM VITALS","Live sync")
          Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){

               MetricCard("Students","38.4k","+14%",Modifier.weight(1f),notify)
               MetricCard("Careers","452","Active",Modifier.weight(1f),notify)
               MetricCard("Universities","54","Campuses",Modifier.weight(1f),notify)
               MetricCard("Grants","$14.2M","34 due",Modifier.weight(1f),notify)
        }

          Spacer(Modifier.height(11.dp))
        SectionTitle("MANAGE REGISTRY","")
          Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){

               RegistryCard("Careers","Salaries & trends","career",Modifier.weight(1f),notify)
               RegistryCard("Universities","ATAR & pathways","university",Modifier.weight(1f),notify)
        }

          Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){

               RegistryCard("Scholarships","Bursaries & dates","scholarship",Modifier.weight(1f),notify)
               RegistryCard("Assessments","RIASEC tests","assessment",Modifier.weight(1f),notify)
        }

          OutlinedButton(onClick={showManager.value=true},modifier=Modifier.fillMaxWidth().padding(top=9.dp)){Text("Manage careers and opportunities")}
  if(showManager.value) AdminOpportunityManager(catalogItems,onSaveOpportunity,onDeleteOpportunity,{showManager.value=false})
    Spacer(Modifier.height(11.dp))
        SectionTitle("PENDING REVIEWS","2")
          ReviewCard("ATAR Cut-Off Update","Cybersecurity cutoff set to 84…","Apex Univ",notify)
          ReviewCard("Clean Energy Grant","$10,000 grant for Year 12 EV te…","Grant",notify)
          Button(onClick={
            notify("System announcement composer opened.")
        },modifier=Modifier.fillMaxWidth().padding(top=10.dp),shape=RoundedCornerShape(11.dp),colors=ButtonDefaults.buttonColors(containerColor=Blue)){
            Text("Broadcast System Announcement",fontSize=12.sp,fontWeight=FontWeight.Bold)
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
                    Text("Add or update career and opportunity information stored on this device.", color = Muted, fontSize = 12.sp)
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
                                Text(item.title, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(item.category, color = Muted, fontSize = 10.sp)
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
                        label = { Text(label, fontSize = 11.sp) },
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
