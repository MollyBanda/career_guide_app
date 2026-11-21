package com.pathfinder.app.ui.screens.profile

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pathfinder.app.data.model.*
import com.pathfinder.app.ui.components.*
import com.pathfinder.app.ui.theme.DashboardColors.Ink
import com.pathfinder.app.ui.theme.DashboardColors.Muted
import com.pathfinder.app.ui.theme.DashboardColors.Blue
import com.pathfinder.app.ui.theme.DashboardColors.Line
import com.pathfinder.app.ui.theme.DashboardColors.Canvas
import com.pathfinder.app.data.validation.PathwiseInputValidation
import com.pathfinder.app.data.repository.PathwiseRepository
import kotlinx.coroutines.launch


@Composable fun ProfileScreen(pad:PaddingValues,onLogout:()->Unit,notify:(String)->Unit,initialName:String,
    initialEmail:String,onOpenDetails:(String,String)->Unit,onProfile:()->Unit,onProfileUpdated:(String,String)->Unit,
    latestAssessment:AssessmentResultEntity?,savedTitles:Set<String> = emptySet(),onOpenSaved:()->Unit = {}){

     val context = LocalContext.current
     val scope = rememberCoroutineScope()
     val repository = remember { PathwiseRepository.get(context) }
     val profileFields = remember(initialEmail) { context.getSharedPreferences("pathwise_profile_fields", 0) }
     val bio = remember(initialEmail) { mutableStateOf(profileFields.getString("$initialEmail.bio", "").orEmpty()) }
     val educationSkills = remember(initialEmail) { mutableStateOf(profileFields.getString("$initialEmail.education_skills", "").orEmpty()) }
     val careerPreferences = remember(initialEmail) { mutableStateOf(profileFields.getString("$initialEmail.career_preferences", "").orEmpty()) }
     LaunchedEffect(initialEmail) {
         runCatching { repository.loadProfileDetails() }.onSuccess { details ->
             details ?: return@onSuccess
             bio.value = details.first
             educationSkills.value = details.second
             careerPreferences.value = details.third
             profileFields.edit()
                 .putString("$initialEmail.bio", details.first)
                 .putString("$initialEmail.education_skills", details.second)
                 .putString("$initialEmail.career_preferences", details.third)
                 .apply()
         }.onFailure {
             notify("Could not load your saved profile details: ${repository.explainAccountError(it)}")
         }
     }
     var editingSection by remember { mutableStateOf<String?>(null) }
     var sectionDraft by remember { mutableStateOf("") }
     val editing=remember {
         mutableStateOf(false)
    }

     val displayName=remember(initialName) {
         mutableStateOf(initialName)
    }

     val displayEmail=remember(initialEmail) {
         mutableStateOf(initialEmail)
    }
     val completionFields = listOf(displayName.value, displayEmail.value, bio.value, educationSkills.value, careerPreferences.value)
     val completionPercent = completionFields.count { it.isNotBlank() } * 100 / completionFields.size

     Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).background(Canvas).padding(horizontal=16.dp,
        vertical=11.dp)){

           DashboardHeader("Profile",onProfile=onProfile)
        Spacer(Modifier.height(15.dp))
          Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,Line)){

                Column(Modifier.fillMaxWidth().padding(12.dp)){

                     Row(verticalAlignment=Alignment.CenterVertically){
                    Surface(color=Color(0xFFD8F7F1),shape=CircleShape){
                        Box(Modifier.size(42.dp),contentAlignment=Alignment.Center){
                            Text(displayName.value.take(1).uppercase(),color=Color(0xFF007C78),fontSize=19.sp,
                                fontWeight=FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)){
                        Text(displayName.value,color=Ink,fontSize=14.sp,fontWeight=FontWeight.Bold)
                        Text(displayEmail.value,color=Muted,fontSize=10.sp)
                    }
                    OutlinedButton(onClick={
                        if(editing.value){
                            when{
                                PathwiseInputValidation.profileNameError(displayName.value) != null ->
                                    notify(PathwiseInputValidation.profileNameError(displayName.value)!!)
                                !PathwiseInputValidation.isValidEmail(displayEmail.value)->notify("Enter a valid email address.")
                                else->{
                                    onProfileUpdated(displayName.value.trim(),displayEmail.value.trim())
                                    editing.value=false
                                }
                            }
                        }
                        else editing.value=true
                    },contentPadding=PaddingValues(horizontal=10.dp,vertical=4.dp),shape=CircleShape){
                        Icon(Icons.Filled.Edit,contentDescription=null,modifier=Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if(editing.value)"Save" else "Edit",fontSize=10.sp)
                    }
                }

                     if(editing.value){
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(displayName.value,{
                        displayName.value=it
                    },modifier=Modifier.fillMaxWidth(),singleLine=true,label={
                        Text("Full name",fontSize=10.sp)
                    },textStyle=LocalTextStyle.current.copy(fontSize=12.sp))
                    Spacer(Modifier.height(5.dp))
                    OutlinedTextField(displayEmail.value,{
                        displayEmail.value=it
                    },modifier=Modifier.fillMaxWidth(),singleLine=true,label={
                        Text("Email",fontSize=10.sp)
                    },readOnly=true,textStyle=LocalTextStyle.current.copy(fontSize=12.sp))
                }

            }

        }

          Spacer(Modifier.height(11.dp))
        Surface(shape=RoundedCornerShape(14.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,Line)){

               Column(Modifier.fillMaxWidth().padding(12.dp)){

                    Row(verticalAlignment=Alignment.CenterVertically){
                    Column(Modifier.weight(1f)){
                        Text("MILESTONE PROGRESS",color=Blue,fontSize=10.sp,fontWeight=FontWeight.Bold)
                        Text("Profile strength",Modifier.padding(top=4.dp),color=Ink,fontSize=12.sp,fontWeight=FontWeight.Bold)
                    }
                    Text("$completionPercent%",color=Color(0xFF007C78),fontSize=22.sp,fontWeight=FontWeight.ExtraBold)
                }

                    LinearProgressIndicator(progress={
                    completionPercent / 100f
                },modifier=Modifier.fillMaxWidth().padding(top=7.dp),color=Color(0xFF008C82),trackColor=Color(0xFFD8F7F1))
                    Row(Modifier.fillMaxWidth().padding(top=8.dp),verticalAlignment=Alignment.CenterVertically){
                    Text(
                        if (completionPercent == 100) "Your profile is complete."
                        else "Complete your personal details, education, skills, and career preferences.",
                        Modifier.weight(1f), color=Muted, fontSize=10.sp
                    )
                }

            }

        }

        if (latestAssessment != null) {
            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, Line)) {
                Column(Modifier.fillMaxWidth().padding(11.dp)) {
                    Text("LATEST ASSESSMENT", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(latestAssessment.resultSummary, Modifier.padding(top = 5.dp), color = Ink, fontSize = 11.sp, lineHeight = 16.sp)
                    if (latestAssessment.recommendations.isNotBlank()) {
                        Text("Suggested paths: " + latestAssessment.recommendations.replace("\n", " · "), Modifier.padding(top = 5.dp), color = Ink, fontSize = 10.sp, lineHeight = 14.sp)
                    }
                    Text("Completed " + java.text.DateFormat.getDateInstance().format(java.util.Date(latestAssessment.completedAt)), Modifier.padding(top = 5.dp), color = Muted, fontSize = 9.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        Text("ACCOUNT & GOALS",color=Blue,fontSize=10.sp,fontWeight=FontWeight.Bold)
           ProfileRow("Personal information",bio.value.ifBlank { "Name, contact details, bio" },"person"){
            editingSection = "Personal information"
            sectionDraft = bio.value
        }

           ProfileRow("Education & skills",educationSkills.value.ifBlank { "Degrees, certifications, languages" },"degree"){
            editingSection = "Education & skills"
            sectionDraft = educationSkills.value
        }

           ProfileRow("Career preferences",careerPreferences.value.ifBlank { "Target roles, salary, locations" },"training"){
            editingSection = "Career preferences"
            sectionDraft = careerPreferences.value
        }

           ProfileRow("Saved opportunities","${savedTitles.size} saved","scholarship"){
            onOpenSaved()
        }

          Spacer(Modifier.height(10.dp))
        Button(onClick={
            notify("You have been logged out.")
            onLogout()
        },modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(11.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFFF0F1),
            contentColor=Color(0xFFC93748))){
            Icon(Icons.Filled.Logout,contentDescription=null,modifier=Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Log out",fontSize=11.sp,fontWeight=FontWeight.Bold)
        }

          Spacer(Modifier.height(8.dp))
    }

     editingSection?.let { section ->
         val hint = when (section) {
             "Personal information" -> "Add a short bio, phone number, or other contact details. Your email is shown on your profile."
             "Education & skills" -> "Add degrees, certifications, skills, and languages. For example: BSc Computing; Kotlin; English and Chichewa."
             else -> "Add target roles, preferred locations, salary range, or work preferences. For example: UX Designer; Lilongwe; remote or hybrid."
         }
         AlertDialog(
             onDismissRequest = { editingSection = null },
             title = { Text(section) },
             text = {
                 OutlinedTextField(
                     value = sectionDraft,
                     onValueChange = { sectionDraft = it },
                     modifier = Modifier.fillMaxWidth(),
                     placeholder = { Text(hint) },
                     minLines = 3,
                     maxLines = 6
                 )
             },
             confirmButton = {
                 TextButton(onClick = {
                     when (section) {
                         "Personal information" -> { bio.value = sectionDraft; profileFields.edit().putString("$initialEmail.bio", sectionDraft).apply() }
                         "Education & skills" -> { educationSkills.value = sectionDraft; profileFields.edit().putString("$initialEmail.education_skills", sectionDraft).apply() }
                         else -> { careerPreferences.value = sectionDraft; profileFields.edit().putString("$initialEmail.career_preferences", sectionDraft).apply() }
                     }
                     editingSection = null
                     scope.launch {
                         runCatching {
                             repository.updateProfileDetails(bio.value, educationSkills.value, careerPreferences.value)
                         }.onSuccess {
                             notify("$section saved to your account.")
                         }.onFailure {
                             notify("$section saved on this device. Cloud sync failed: ${repository.explainAccountError(it)}")
                         }
                     }
                 }) { Text("Save") }
             },
             dismissButton = { TextButton(onClick = { editingSection = null }) { Text("Cancel") } }
         )
     }

}
@Composable private fun ProfileRow(title:String,subtitle:String,icon:String,onClick:()->Unit){

     Surface(Modifier.fillMaxWidth().padding(top=7.dp).clickable(onClick=onClick),shape=RoundedCornerShape(11.dp),
        color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,Line)){

          Row(Modifier.padding(9.dp),verticalAlignment=Alignment.CenterVertically){
            FeatureIcon(icon)
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)){
                Text(title,color=Ink,fontSize=11.sp,fontWeight=FontWeight.Bold)
                Text(subtitle,color=Muted,fontSize=9.sp,maxLines=1)
            }
        }

    }

}
