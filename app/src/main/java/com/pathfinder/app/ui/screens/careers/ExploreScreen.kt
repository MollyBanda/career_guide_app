package com.pathfinder.app.ui.screens.careers

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


@Composable fun ExploreScreen(pad:PaddingValues,notify:(String)->Unit,onOpenDetails:(String,String,String)->Unit,
    onProfile:()->Unit,catalogItems:List<OpportunityEntity>){

     val query=remember {
         mutableStateOf("")
    }

     val selectedFilter=remember {
         mutableStateOf("All Matches")
    }

     val careers=catalogItems.filter { it.category != "Assessment" }.map { item ->
        Triple(iconName(item.category,item.title),item.title,item.subtitle)
    }
     val visibleCareers=careers.filter {
         item ->
          val matchesQuery=item.second.contains(query.value,ignoreCase=true) || item.third.contains(query.value,
            ignoreCase=true)
          val matchesFilter=when(selectedFilter.value){
            "Tech & Design"->item.third.contains("Technology") || item.third.contains("Design")
            "Science"->item.third.contains("Science")
            else->true
        }

          matchesQuery && matchesFilter
    }

     Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).background(Canvas).padding(horizontal=16.dp,
        vertical=11.dp)){

           DashboardHeader("Explore",onProfile=onProfile)
        Spacer(Modifier.height(14.dp))
           OutlinedTextField(value=query.value,onValueChange={
            query.value=it
        },modifier=Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(12.dp),placeholder={
            Text("Search careers and opportunities",fontSize=12.sp,color=Muted)
        },leadingIcon={
            Icon(Icons.Filled.Search,contentDescription="Search careers",tint=Muted)
        },trailingIcon={
            if(query.value.isNotEmpty()) IconButton(onClick={
                query.value=""
            }){
                Text("×",fontSize=20.sp,color=Muted)
            }
        },colors=OutlinedTextFieldDefaults.colors(unfocusedContainerColor=Color.White,focusedContainerColor=Color.White,
            unfocusedBorderColor=Line,focusedBorderColor=Blue))
           Spacer(Modifier.height(13.dp))
        Row(verticalAlignment=Alignment.CenterVertically){
            Text("TOP MATCHES",color=Blue,fontSize=10.sp,fontWeight=FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${visibleCareers.size} results",color=Muted,fontSize=10.sp)
        }

          Text("Careers to explore",Modifier.padding(top=4.dp,bottom=9.dp),color=Ink,fontSize=22.sp,fontWeight=FontWeight.ExtraBold)
          Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
            listOf("All Matches","Tech & Design","Science").forEach {
                 filter->FilterPill(filter,selectedFilter.value==filter){
                    selectedFilter.value=filter
                }
            }
        }

          Spacer(Modifier.height(9.dp))
          if(visibleCareers.isEmpty()) Text("No careers match that search yet.",Modifier.padding(vertical=18.dp),
            color=Muted,fontSize=12.sp)
           visibleCareers.forEach {
             item->val bits=item.third.split(" · ")
            val category=when{
                item.second.contains("Scholarship",true)->"Scholarships"
                item.second.contains("Intern",true)->"Internships"
                item.second.contains("University",true)->"Universities"
                item.second.contains("Training",true)->"Training"
                item.second.contains("Analytics",true)->"Training"
                else->"Careers"
            }
            ExploreCareerCard(item.first,item.second,bits.first(),bits.last()){
                onOpenDetails(item.second,item.third,category)
            }
        }

    }

}
@Composable private fun ExploreCareerCard(icon:String,title:String,subtitle:String,match:String,onClick:()->Unit){

     Surface(Modifier.fillMaxWidth().padding(top=7.dp).clickable(onClick=onClick),shape=RoundedCornerShape(12.dp),
        color=Color.White,border=BorderStroke(1.dp,Line)){

          Row(Modifier.padding(horizontal=10.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically){
            FeatureIcon(icon)
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)){
                Text(title,color=Ink,fontSize=12.sp,fontWeight=FontWeight.Bold)
                Text(subtitle,color=Muted,fontSize=10.sp)
            }
            Label(match,Color(0xFFD8F7F1),Color(0xFF007C78))
            Spacer(Modifier.width(3.dp))
            Icon(Icons.Filled.ChevronRight,contentDescription="View $title",tint=Muted,modifier=Modifier.size(19.dp))
        }

    }

}
