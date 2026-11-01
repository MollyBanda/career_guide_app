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


@Composable fun ProfileScreen(pad:PaddingValues,onLogout:()->Unit,notify:(String)->Unit,initialName:String,
    initialEmail:String,onOpenDetails:(String,String)->Unit,onProfile:()->Unit,onProfileUpdated:(String,String)->Unit,latestAssessment:AssessmentResultEntity?){

     val editing=remember {
         mutableStateOf(false)
    }

     val displayName=remember(initialName) {
         mutableStateOf(initialName)
    }

     val displayEmail=remember(initialEmail) {
         mutableStateOf(initialEmail)
    }

     Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).background(Canvas).padding(horizontal=16.dp,
        vertical=11.dp)){

           DashboardHeader("Profile",onProfile=onProfile)
        Spacer(Modifier.height(15.dp))
          Surface(shape=RoundedCornerShape(14.dp),color=Color.White,border=BorderStroke(1.dp,Line)){

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
                        Text("${displayName.value}  ✓",color=Ink,fontSize=14.sp,fontWeight=FontWeight.Bold)
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
        Surface(shape=RoundedCornerShape(14.dp),color=Color.White,border=BorderStroke(1.dp,Line)){

               Column(Modifier.fillMaxWidth().padding(12.dp)){

                    Row(verticalAlignment=Alignment.CenterVertically){
                    Column(Modifier.weight(1f)){
                        Text("MILESTONE PROGRESS",color=Blue,fontSize=10.sp,fontWeight=FontWeight.Bold)
                        Text("Profile strength",Modifier.padding(top=4.dp),color=Ink,fontSize=12.sp,fontWeight=FontWeight.Bold)
                    }
                    Text("70%",color=Color(0xFF007C78),fontSize=22.sp,fontWeight=FontWeight.ExtraBold)
                }

                    LinearProgressIndicator(progress={
                    0.70f
                },modifier=Modifier.fillMaxWidth().padding(top=7.dp),color=Color(0xFF008C82),trackColor=Color(0xFFD8F7F1))
                    Row(Modifier.fillMaxWidth().padding(top=8.dp),verticalAlignment=Alignment.CenterVertically){
                    Text("Add your skills to improve career matches.",Modifier.weight(1f),color=Muted,fontSize=10.sp)
                    TextButton(onClick={
                        notify("Add your skills to improve your matches.")
                    },contentPadding=PaddingValues(horizontal=7.dp,vertical=2.dp)){
                        Text("+ Add 3 Skills",fontSize=10.sp)
                    }
                }

            }

        }

        if (latestAssessment != null) {
            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = Color.White, border = BorderStroke(1.dp, Line)) {
                Column(Modifier.fillMaxWidth().padding(11.dp)) {
                    Text("LATEST ASSESSMENT", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(latestAssessment.resultSummary, Modifier.padding(top = 5.dp), color = Ink, fontSize = 11.sp, lineHeight = 16.sp)
                    Text("Completed " + java.text.DateFormat.getDateInstance().format(java.util.Date(latestAssessment.completedAt)), Modifier.padding(top = 5.dp), color = Muted, fontSize = 9.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        Text("ACCOUNT & GOALS",color=Blue,fontSize=10.sp,fontWeight=FontWeight.Bold)
           ProfileRow("Personal information","Name, contact details, bio","person"){
            onOpenDetails("Personal information","${displayName.value} · ${displayEmail.value}")
        }

           ProfileRow("Education & skills","Degrees, certifications, languages","degree"){
            onOpenDetails("Education & skills","Add your education, certifications, and languages.")
        }

           ProfileRow("Career preferences","Target roles, salary, locations","training"){
            onOpenDetails("Career preferences","Choose target roles, salary expectations, and preferred locations.")
        }

           ProfileRow("Saved opportunities","${initialName}'s saved opportunities","scholarship"){
            onOpenDetails("Saved opportunities","Your bookmarked careers, courses, and scholarships are shown on the Saved page.")
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

          Text("PathFinder v2.4.0 · Built for your journey",Modifier.align(Alignment.CenterHorizontally).padding(top=12.dp),
            color=Muted,fontSize=9.sp)
    }

}
@Composable private fun ProfileRow(title:String,subtitle:String,icon:String,onClick:()->Unit){

     Surface(Modifier.fillMaxWidth().padding(top=7.dp).clickable(onClick=onClick),shape=RoundedCornerShape(11.dp),
        color=Color.White,border=BorderStroke(1.dp,Line)){

          Row(Modifier.padding(9.dp),verticalAlignment=Alignment.CenterVertically){
            FeatureIcon(icon)
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)){
                Text(title,color=Ink,fontSize=11.sp,fontWeight=FontWeight.Bold)
                Text(subtitle,color=Muted,fontSize=9.sp)
            }
            Icon(Icons.Filled.ChevronRight,contentDescription="Open $title",tint=Muted,modifier=Modifier.size(19.dp))
        }

    }

}
