package com.pathfinder.app.ui.screens.home

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


@Composable fun HomeScreen(pad:PaddingValues,openSaved:()->Unit,notify:(String)->Unit,openCategory:(String)->Unit,
    savedTitles:Set<String>,onToggleSave:(String)->Unit,startQuiz:()->Unit,onOpenDetails:(String,String,String)->Unit,
    onProfile:()->Unit){

     Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).background(Canvas).padding(horizontal=16.dp,
        vertical=11.dp)){

           DashboardHeader("Home",onProfile=onProfile)
        Spacer(Modifier.height(15.dp))
          Label("✦  EXPLORE YOUR OPTIONS",Color(0xFFE9EEFF),Blue)
          Text("Where do you want to go?",Modifier.padding(top=7.dp),color=Ink,fontSize=24.sp,lineHeight=28.sp,
            fontWeight=FontWeight.ExtraBold)
          Text("Find paths that match your passions and strengths",Modifier.padding(top=3.dp,bottom=10.dp),
            color=Muted,fontSize=11.sp)
           val options=listOf(Triple("career","Careers","240+ paths"),Triple("university","Universities","85 institutions"),
            Triple("scholarship","Scholarships","Funding options"),Triple("training","Training","Build your skills"),
            Triple("assessment","Assessment","Discover strengths"),Triple("internship","Internships","Gain experience"))
          options.chunked(2).forEach{
            row->Row(Modifier.fillMaxWidth().padding(bottom=7.dp),horizontalArrangement=Arrangement.spacedBy(7.dp)){

                    row.forEach{
                    item->Surface(Modifier.weight(1f).clickable{
                        openCategory(when(item.second){
                            "Universities"->"Universities"
                            "Scholarships"->"Scholarships"
                            "Training"->"Training"
                            "Assessment"->"Assessment"
                            "Internships"->"Internships"
                            else->"Careers"
                        })
                    },shape=RoundedCornerShape(11.dp),color=Color.White,border=BorderStroke(1.dp,Line)){

                             Column(Modifier.padding(9.dp)){
                            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                                FeatureIcon(item.first)
                                Text("›",color=Muted)
                            }

                                 Text(item.second,Modifier.padding(top=5.dp),color=Ink,fontSize=12.sp,fontWeight=FontWeight.Bold)
                            Text(item.third,color=Muted,fontSize=10.sp)
                        }

                    }
                }

            }
        }

           Row(Modifier.fillMaxWidth().padding(top=5.dp,bottom=6.dp),verticalAlignment=Alignment.CenterVertically){
            Icon(Icons.Filled.AutoAwesome,contentDescription=null,tint=Blue,modifier=Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text("Your top career match",color=Ink,fontSize=12.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("See all ›",color=Blue,fontSize=10.sp)
        }

           Surface(Modifier.clickable{
            onOpenDetails("UX Designer","Design & Technology","Careers")
        },shape=RoundedCornerShape(12.dp),color=Color.White,border=BorderStroke(1.dp,Line)){
            Column(Modifier.padding(10.dp)){

                    Row(verticalAlignment=Alignment.CenterVertically){
                    FeatureIcon("ux")
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)){
                        Text("UX Designer",color=Ink,fontSize=12.sp,fontWeight=FontWeight.Bold)
                        Text("Design & Technology",color=Muted,fontSize=10.sp)
                    }
                    Label("94%",Color(0xFFD8F7F1),Color(0xFF007C78))
                }

                   Text("↗  +16% Growth                        ◉  $78k–$120k",Modifier.padding(top=8.dp),color=Muted,
                    fontSize=10.sp)
            }
        }

           Row(Modifier.fillMaxWidth().padding(top=12.dp,bottom=6.dp),verticalAlignment=Alignment.CenterVertically){
            Text("Opportunities for you",color=Ink,fontSize=12.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("View more ›",Modifier.clickable(onClick=openSaved),color=Blue,fontSize=10.sp)
        }

            Opportunity("scholarship","Future Leaders Scholarship","Full tuition · Scholarship",{
            onOpenDetails("Future Leaders Scholarship","Full tuition · Scholarship","Scholarships")
        },"Future Leaders Scholarship" in savedTitles,onToggleSave)
        Spacer(Modifier.height(6.dp))
        Opportunity("degree","Bachelor of Science in Computer Science","University program · 3–4 years",{
            onOpenDetails("Bachelor of Science in Computer Science","University program · 3–4 years","Universities")
        },"Bachelor of Science in Computer Science" in savedTitles,onToggleSave)
          Spacer(Modifier.height(9.dp))
           Surface(Modifier.fillMaxWidth().clickable(onClick=startQuiz),shape=RoundedCornerShape(12.dp),color=Blue){
            Row(Modifier.padding(11.dp),verticalAlignment=Alignment.CenterVertically){

                   Column(Modifier.weight(1f)){
                    Text("DAILY DISCOVERY",color=Color(0xFFBFD3FF),fontSize=9.sp,fontWeight=FontWeight.Bold)
                    Text("Take the 3-min career quiz",color=Color.White,fontSize=12.sp,fontWeight=FontWeight.Bold)
                    Text("Refresh recommendations with new interests",color=Color.White,fontSize=10.sp)
                }

                    Button(onClick=startQuiz,contentPadding=PaddingValues(horizontal=11.dp,vertical=5.dp),
                    shape=CircleShape,colors=ButtonDefaults.buttonColors(containerColor=Color.White,contentColor=Blue)){
                    Text("Start Quiz  →",fontSize=10.sp,fontWeight=FontWeight.Bold)
                }

            }
        }

    }

}
