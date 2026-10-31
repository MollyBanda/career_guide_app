package com.pathfinder.app.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
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
import androidx.compose.runtime.Composable
import com.pathfinder.app.ui.theme.DashboardColors.Blue
import com.pathfinder.app.ui.theme.DashboardColors.Ink
import com.pathfinder.app.ui.theme.DashboardColors.Line
import com.pathfinder.app.ui.theme.DashboardColors.Muted

@Composable fun FeatureIcon(name:String){

     val glyph=when(name){

          "career"->Icons.Filled.Work
          "university"->Icons.Filled.AccountBalance
          "scholarship"->Icons.Filled.EmojiEvents
          "training"->Icons.Filled.TrackChanges
          "assessment"->Icons.Filled.Psychology
          "internship"->Icons.Filled.BusinessCenter
          "degree"->Icons.Filled.School
          "person"->Icons.Filled.Person
          "ux"->Icons.Filled.DesignServices
          "data"->Icons.Filled.ShowChart
          else->Icons.Filled.Work
    }

     val (bg,tint)=when(name){

          "scholarship"->Color(0xFFFFF0D8) to Color(0xFFB96A13)
          "training","internship"->Color(0xFFD9F7F2) to Color(0xFF008C82)
          "assessment"->Color(0xFFE8EDFF) to Blue
          else->Color(0xFFE8EDFF) to Blue
    }

     Surface(color=bg,shape=RoundedCornerShape(8.dp)){

          Box(Modifier.size(30.dp),contentAlignment=Alignment.Center){
            Icon(glyph,contentDescription=name,tint=tint,modifier=Modifier.size(17.dp))
        }

    }

}
@Composable fun DashboardHeader(page:String,onBack:(()->Unit)?=null,onProfile:()->Unit={
}){

     Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){

           if(onBack!=null){
            IconButton(onClick=onBack,modifier=Modifier.size(38.dp)){
                Icon(Icons.Filled.ArrowBack,contentDescription="Back",tint=Ink)
            }
            Spacer(Modifier.width(4.dp))
        }

          Surface(color=Blue,shape=RoundedCornerShape(9.dp)){
            Box(Modifier.size(31.dp),contentAlignment=Alignment.Center){
                Text("⌁",color=Color.White,fontSize=21.sp,fontWeight=FontWeight.Bold)
            }
        }

          Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)){
            Text("PATHFINDER",color=Blue,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=.7.sp)
            Text(page,color=Ink,fontSize=15.sp,fontWeight=FontWeight.Bold)
        }

           Icon(Icons.Filled.Notifications,contentDescription="Notifications",tint=Muted,modifier=Modifier.size(22.dp))
        Spacer(Modifier.width(9.dp))
           Surface(Modifier.clickable(onClick=onProfile),color=Color(0xFFDBF5EE),shape=CircleShape){
            Text("M",Modifier.padding(horizontal=9.dp,vertical=6.dp),color=Color(0xFF007C78),fontSize=12.sp,
                fontWeight=FontWeight.Bold)
        }

    }

}
@Composable fun PinnedBadge(){

     Surface(color=Color(0xFFD8F7F1),shape=RoundedCornerShape(8.dp)){

          Row(Modifier.padding(horizontal=8.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically){

               Icon(Icons.Filled.BookmarkBorder,contentDescription="Pinned",tint=Color(0xFF007C78),modifier=Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text("2 pinned",color=Color(0xFF007C78),fontSize=10.sp,fontWeight=FontWeight.Bold)
        }

    }

}
@Composable fun Label(text:String,bg:Color,fg:Color){
    Surface(color=bg,shape=RoundedCornerShape(8.dp)){
        Text(text,Modifier.padding(horizontal=8.dp,vertical=6.dp),color=fg,fontSize=10.sp,fontWeight=FontWeight.Bold)
    }
}
@Composable fun Pill(text:String,active:Boolean=false){
    Surface(shape=CircleShape,color=if(active) Blue else Color.White,border=BorderStroke(1.dp,if(active) Blue else Line)){
        Text(text,Modifier.padding(horizontal=8.dp,vertical=6.dp),color=if(active) Color.White else Muted,
            fontSize=10.sp,fontWeight=FontWeight.SemiBold)
    }
}
fun iconName(category:String,title:String):String=when(category){
    "Universities"->"university"
    "Scholarships"->"scholarship"
    "Training"->"training"
    "Assessment"->"assessment"
    "Internships"->"internship"
    else->when{title.contains("data",true)->"data";title.contains("design",true)->"ux";title.contains("health",true)->"person";else->"career"}
}

@Composable fun Opportunity(icon:String,title:String,subtitle:String,click:()->Unit,isSaved:Boolean,
    onToggleSave:(String)->Unit){

     Surface(Modifier.fillMaxWidth().clickable(onClick=click),shape=RoundedCornerShape(10.dp),color=Color.White,
        border=BorderStroke(1.dp,Line)){

            Row(Modifier.padding(9.dp),verticalAlignment=Alignment.CenterVertically){
            FeatureIcon(icon)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f).clickable(onClick=click)){
                Text(title,color=Ink,fontSize=11.sp,fontWeight=FontWeight.Bold)
                Text(subtitle,color=Muted,fontSize=10.sp)
            }
            IconButton(onClick={
                onToggleSave(title)
            },modifier=Modifier.size(32.dp)){
                Icon(if(isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,contentDescription=if(isSaved) "Remove $title from saved" else "Save $title",
                    tint=Blue,modifier=Modifier.size(19.dp))
            }
        }

    }

}

@Composable fun FilterPill(label:String,selected:Boolean,onClick:()->Unit){

     Surface(Modifier.clickable(onClick=onClick),shape=CircleShape,color=if(selected) Blue else Color.White,
        border=BorderStroke(1.dp,if(selected) Blue else Line)){

          Text(label,Modifier.padding(horizontal=9.dp,vertical=7.dp),color=if(selected) Color.White else Muted,
            fontSize=10.sp,fontWeight=FontWeight.SemiBold)
    }

}
