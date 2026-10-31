package com.pathfinder.app.ui.screens.counsellor

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


@Composable fun CounsellorDashboardScreen(pad:PaddingValues,notify:(String)->Unit,onProfile:()->Unit){

     Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).background(Canvas).padding(horizontal=15.dp,
        vertical=10.dp)){

           DashboardHeader("Counsellor Dashboard",onProfile=onProfile)
        Spacer(Modifier.height(10.dp))
          Surface(shape=RoundedCornerShape(13.dp),color=Color.White,border=BorderStroke(1.dp,Line)){

               Row(Modifier.fillMaxWidth().padding(11.dp),verticalAlignment=Alignment.CenterVertically){
                Text("👩🏽‍💼",fontSize=28.sp)
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)){
                    Text("Dr. Brenda Vance  ✓",color=Ink,fontSize=13.sp,fontWeight=FontWeight.Bold)
                    Text("Yr 11–12 Cohort Lead",color=Muted,fontSize=10.sp)
                }
                IconButton(onClick={
                    notify("Counsellor settings opened.")
                }){
                    Icon(Icons.Filled.FilterList,contentDescription="Dashboard settings",tint=Muted)
                }
            }

        }

          Spacer(Modifier.height(9.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){

               MetricCard("Caseload","42","Active",Modifier.weight(1f),notify)
               MetricCard("Today","6","In 18m",Modifier.weight(1f),notify)
               MetricCard("Placement","89%","Confirmed",Modifier.weight(1f),notify)
        }

          Spacer(Modifier.height(10.dp))
        SectionTitle("NEXT SESSION","18m")
          ActionCard("Marcus Chen (Yr 12)","Engineering vs Clean Energy","10:15 AM · 30 min", "Join",notify)
          Spacer(Modifier.height(11.dp))
        SectionTitle("ATTENTION NEEDED","2")
          ActionCard("Sarah Jenkins","Scholarship review · 3d left","Scholarship application","Review",notify)
          ActionCard("Liam Zhao","ATAR prerequisite mismatch","Course planning","Bridge",notify)
          Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){

               QuickAction("Blueprint",Icons.Filled.AutoAwesome,Modifier.weight(1f),notify)
               QuickAction("Log Note",Icons.Filled.Edit,Modifier.weight(1f),notify)
               QuickAction("Grants",Icons.Filled.EmojiEvents,Modifier.weight(1f),notify)
        }

          Spacer(Modifier.height(11.dp))
        SectionTitle("TODAY'S SCHEDULE","2 remaining")
          ScheduleRow("10:15 AM","Marcus Chen","Course Mapping",notify)
          ScheduleRow("11:30 AM","Priya Patel","Career Fit Review",notify)
    }

}
