package com.pathfinder.app.ui.screens.dashboard

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


import com.pathfinder.app.ui.screens.admin.AdminDashboardScreen
import com.pathfinder.app.ui.screens.assessment.AssessmentScreen
import com.pathfinder.app.ui.screens.careers.ExploreScreen
import com.pathfinder.app.ui.screens.careers.CareerDetailsScreen
import com.pathfinder.app.ui.screens.categories.CategoryScreen
import com.pathfinder.app.ui.screens.counsellor.CounsellorDashboardScreen
import com.pathfinder.app.ui.screens.home.HomeScreen
import com.pathfinder.app.ui.screens.profile.ProfileScreen
import com.pathfinder.app.ui.screens.saved.SavedScreen

@Composable
fun DashboardScreen(page:String,onNavigate:(String)->Unit,notify:(String)->Unit,role:String="Student",profileName:String="Maya Martinez",
    profileEmail:String="maya.m@example.com",savedTitles:Set<String> = emptySet(),detailTitle:String="",detailSubtitle:String="",
    detailCategory:String="Careers",detailReturn:String="Home",onOpenDetails:(String,String,String,String)->Unit={
    _,_,_,_->
},onToggleSave:(String)->Unit={
},onProfileUpdated:(String,String)->Unit={
    _,_->
},
    catalogItems:List<OpportunityEntity> = emptyList(),
    universityProgrammes:List<UniversityProgrammeEntity> = emptyList(),
    latestAssessment:AssessmentResultEntity?=null,
    onSaveOpportunity:(OpportunityEntity)->Unit={
    },
    onDeleteOpportunity:(OpportunityEntity)->Unit={
    },
    onQuizCompleted:(String,List<String>)->Unit={
    _,_->
},
    studentAccounts:List<PathwiseStudentAccount> = emptyList(),
    onBroadcastAnnouncement:(String,String)->Unit={_,_->
}){

     val saved=page=="Saved"
     Scaffold(containerColor = Color.Transparent, bottomBar = {
        AppBottomNavigation(page = page, onNavigate = onNavigate)
    }) {
        pad->when(page){

                "Saved"->SavedScreen(pad,{
                onNavigate("Home")
            },{
                onNavigate("Explore")
            },savedTitles,onToggleSave,{
                title,sub,cat->onOpenDetails(title,sub,cat,"Saved")
            },{
                onNavigate("Profile")
            },catalogItems)
                "Explore"->ExploreScreen(pad,notify,{
                title,sub,cat->onOpenDetails(title,sub,cat,"Explore")
            },{
                onNavigate("Profile")
            },catalogItems,savedTitles,onToggleSave)
                  "Profile"->ProfileScreen(pad,{
                onNavigate("SignIn")
            },notify,profileName,profileEmail,{
                title,sub->onOpenDetails(title,sub,"Profile","Profile")
            },{
                onNavigate("Profile")
            },onProfileUpdated,latestAssessment,savedTitles,{
                onNavigate("Saved")
            })
                 "Quiz"->AssessmentScreen(pad,{
                onNavigate("Home")
            },notify,{
                onNavigate("Profile")
            },onQuizCompleted)
                 "Details"->CareerDetailsScreen(pad,detailTitle,detailSubtitle,detailCategory,savedTitles,onToggleSave,
                {
                onNavigate(detailReturn)
            },{
                onNavigate("Quiz")
            },{
                onNavigate("Profile")
            },catalogItems,universityProgrammes)
                 in listOf("Careers","Universities","Scholarships","Training","Assessment","Internships")->CategoryScreen(pad,
                page,{
                onNavigate("Home")
            },notify,{
                title,sub->onOpenDetails(title,sub,page,page)
            },{
                onNavigate("Profile")
            },catalogItems,{
                onNavigate("Quiz")
            })
                else->when(role){

                      "Admin"->AdminDashboardScreen(pad,notify,{
                    onNavigate("Profile")
                },catalogItems,onSaveOpportunity,onDeleteOpportunity,studentAccounts,onBroadcastAnnouncement)
                      "Counsellor"->CounsellorDashboardScreen(pad,notify,{
                    onNavigate("Profile")
                })
                      else->HomeScreen(pad,{
                    onNavigate("Saved")
            },notify,{
                    category->onNavigate(category)
                },savedTitles,onToggleSave,{
                    onNavigate("Quiz")
                },{
                    title,sub,cat->onOpenDetails(title,sub,cat,"Home")
                },{
                    onNavigate("Profile")
            },{
                onNavigate("Explore")
            })
            }

        }
    }

}
