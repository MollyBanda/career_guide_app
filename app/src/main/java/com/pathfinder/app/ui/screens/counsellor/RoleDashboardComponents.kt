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
import com.pathfinder.app.ui.theme.DashboardColors.Ink
import com.pathfinder.app.ui.theme.DashboardColors.Muted
import com.pathfinder.app.ui.theme.DashboardColors.Blue
import com.pathfinder.app.ui.theme.DashboardColors.Line


@Composable fun MetricCard(label:String,value:String,detail:String,modifier:Modifier,notify:(String)->Unit){

     Surface(modifier.clickable{
        notify("$label metrics opened.")
    },shape=RoundedCornerShape(10.dp),color=Color.White,border=BorderStroke(1.dp,Line)){

          Column(Modifier.padding(horizontal=7.dp,vertical=8.dp)){
            Text(label,color=Ink,fontSize=13.sp,maxLines=2)
            Text(value,color=Ink,fontSize=20.sp,fontWeight=FontWeight.ExtraBold)
            Text(detail,color=Color(0xFF00685F),fontSize=12.sp,fontWeight=FontWeight.SemiBold)
        }

    }

}
@Composable fun SectionTitle(title:String,trailing:String){

     Row(Modifier.fillMaxWidth().padding(bottom=6.dp),verticalAlignment=Alignment.CenterVertically){
        Text(title,color=Ink,fontSize=14.sp,fontWeight=FontWeight.ExtraBold)
        Spacer(Modifier.weight(1f))
        Text(trailing,color=Ink,fontSize=12.sp)
    }

}
@Composable fun ActionCard(title:String,description:String,meta:String,action:String,onAction:()->Unit){

     Surface(Modifier.fillMaxWidth().padding(bottom=6.dp),shape=RoundedCornerShape(11.dp),color=Color.White,
        border=BorderStroke(1.dp,Line)){

          Row(Modifier.padding(horizontal=9.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f)){
                Text(title,color=Ink,fontSize=14.sp,fontWeight=FontWeight.Bold)
                Text(description,color=Ink,fontSize=12.sp)
                Text(meta,color=Muted,fontSize=12.sp)
            }
            Button(onClick=onAction,contentPadding=PaddingValues(horizontal=11.dp,vertical=5.dp),shape=RoundedCornerShape(8.dp),
                colors=ButtonDefaults.buttonColors(containerColor=Blue)){
                Text(action,fontSize=12.sp,fontWeight=FontWeight.Bold)
            }
        }

    }

}
@Composable fun QuickAction(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier,
    notify:(String)->Unit){

     OutlinedButton(onClick={
        notify("$label opened.")
    },modifier=modifier.height(42.dp),contentPadding=PaddingValues(horizontal=4.dp),shape=RoundedCornerShape(9.dp),
        border=BorderStroke(1.dp,Line)){
        Icon(icon,contentDescription=null,modifier=Modifier.size(14.dp),tint=Blue)
        Spacer(Modifier.width(4.dp))
        Text(label,fontSize=9.sp,maxLines=1)
    }

}
@Composable fun ScheduleRow(time:String,name:String,appointment:String,onClick:()->Unit){

     Row(Modifier.fillMaxWidth().clickable(onClick=onClick).padding(vertical=7.dp),verticalAlignment=Alignment.CenterVertically){
        Text(time,color=Blue,fontSize=10.sp,fontWeight=FontWeight.Bold,modifier=Modifier.width(68.dp))
        Column(Modifier.weight(1f)){
            Text(name,color=Ink,fontSize=10.sp,fontWeight=FontWeight.Bold)
            Text(appointment,color=Muted,fontSize=9.sp)
        }
    }

}
@Composable fun RegistryCard(title:String,subtitle:String,icon:String,modifier:Modifier,onClick:()->Unit){

     Surface(modifier.fillMaxWidth().padding(bottom=6.dp).clickable(onClick=onClick),shape=RoundedCornerShape(10.dp),color=Color.White,border=BorderStroke(1.dp,Line)){

          Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically){
            FeatureIcon(icon)
            Spacer(Modifier.width(7.dp))
            Column(Modifier.weight(1f)){
                Text(title,color=Ink,fontSize=14.sp,fontWeight=FontWeight.Bold,maxLines=2)
                Text(subtitle,color=Ink,fontSize=12.sp,maxLines=2)
            }
        }

    }

}
@Composable fun ReviewCard(title:String,description:String,tag:String,onReview:()->Unit,onApprove:()->Unit){

     Surface(Modifier.fillMaxWidth().padding(bottom=6.dp),shape=RoundedCornerShape(10.dp),color=Color.White,
        border=BorderStroke(1.dp,Line)){

          Column(Modifier.fillMaxWidth().padding(9.dp)){
            Row(verticalAlignment=Alignment.Top){
                FeatureIcon("degree")
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)){
                    Text(title,color=Ink,fontSize=14.sp,fontWeight=FontWeight.Bold,maxLines=2)
                    Text(description,color=Ink,fontSize=12.sp,lineHeight=17.sp)
                    Label(tag,Color(0xFFE8EDFF),Blue)
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End,verticalAlignment=Alignment.CenterVertically){
                TextButton(onClick=onReview,contentPadding=PaddingValues(horizontal=8.dp)){
                    Text("View",fontSize=13.sp)
                }
                Button(onClick=onApprove,contentPadding=PaddingValues(horizontal=10.dp,vertical=4.dp),shape=RoundedCornerShape(7.dp)){
                    Text("Approve",fontSize=13.sp,fontWeight=FontWeight.Bold)
                }
            }
        }

    }

}
