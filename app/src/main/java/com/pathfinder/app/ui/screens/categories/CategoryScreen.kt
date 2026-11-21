package com.pathfinder.app.ui.screens.categories

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
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


@Composable fun CategoryScreen(pad:PaddingValues,page:String,onBack:()->Unit,notify:(String)->Unit,onOpenDetails:(String,
    String)->Unit,onProfile:()->Unit,catalogItems:List<OpportunityEntity>){

     val entries=catalogItems.filter { item ->
        item.category == page && (page!="Universities" || item.id=="uni-malawi" || item.id.startsWith("uni-expanded-") || item.title=="University of Malawi")
    }.map { item ->
        Triple(iconName(item.category,item.title),item.title,item.subtitle)
    }.let { rows -> if(page=="Universities") rows.sortedWith(compareBy({it.third.substringBefore(" ·")},{it.second})) else rows }

     val query=remember {
         mutableStateOf("")
    }

     val selectedFilter=remember {
         mutableStateOf("All")
    }

     val visibleEntries=entries.filter {
         (_,title,subtitle)->
          val matchesQuery=title.contains(query.value,ignoreCase=true)||subtitle.contains(query.value,ignoreCase=true)
          val matchesFilter=when {
              selectedFilter.value=="All" -> true
              page=="Universities" -> subtitle.substringBefore(" · ").equals(selectedFilter.value,ignoreCase=true)
              else -> subtitle.contains(selectedFilter.value,ignoreCase=true)
          }
          matchesQuery&&matchesFilter
    }

     Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).background(Canvas).padding(horizontal=16.dp,
        vertical=12.dp)){

          DashboardHeader(page,onBack=onBack,onProfile=onProfile)
          Spacer(Modifier.height(14.dp))
          OutlinedTextField(value=query.value,onValueChange={
            query.value=it
        },modifier=Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(11.dp),placeholder={
            Text("Search $page",fontSize=12.sp)
        },leadingIcon={
            Icon(Icons.Filled.Search,contentDescription="Search $page",tint=Muted)
        },trailingIcon={
            if(query.value.isNotEmpty())IconButton(onClick={
                query.value=""
            }){
                Text("×",fontSize=20.sp)
            }
        })
          if(page=="Careers" || page=="Universities"){
            Spacer(Modifier.height(8.dp))
            val filters=if(page=="Universities") listOf("All")+entries.map { it.third.substringBefore(" · ") }.filter { it.isNotBlank() }.distinct().sorted()
                else listOf("All","Technology","Health Science","Design")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                filters.forEach{
                    filter->FilterPill(filter,selectedFilter.value==filter){
                        selectedFilter.value=filter
                    }
                }
            }
        }

          Spacer(Modifier.height(14.dp))
        Label("PATHFINDER PICKS",Color(0xFFE8EDFF),Blue)
          Text(when(page){
            "Careers"->"Find a career that fits"
            "Universities"->"Explore universities"
            "Scholarships"->"Funding for your future"
            "Training"->"Build your skills"
            "Assessment"->"Know your strengths"
            else->"Get real experience"
        },Modifier.padding(top=9.dp),color=Ink,fontSize=22.sp,fontWeight=FontWeight.ExtraBold)
          Text(when(page){
            "Careers"->"Explore roles matched to your interests and goals."
            "Universities"->"Compare study destinations and pathways."
            "Scholarships"->"Find funding opportunities for your next step."
            "Training"->"Learn practical skills at your own pace."
            "Assessment"->"Take a short assessment and get useful insights."
            else->"Build experience while exploring your career path."
        },Modifier.padding(top=4.dp,bottom=10.dp),color=Muted,fontSize=11.sp,lineHeight=16.sp)
          if(visibleEntries.isEmpty()) Text(if(page=="Careers")"No careers found" else "No ${page.lowercase()} found",
            Modifier.padding(vertical=18.dp),color=Muted,fontSize=12.sp)
          if(page=="Universities") {
              visibleEntries.groupBy { it.third.substringBefore(" ·") }.toSortedMap().forEach { (country, universities) ->
                  Text(country,Modifier.padding(top=13.dp,bottom=2.dp),color=Blue,fontSize=15.sp,fontWeight=FontWeight.Bold)
                  universities.forEach { (icon,title,subtitle) ->
                      CatalogueCard(icon,title,subtitle) { onOpenDetails(title,subtitle) }
                  }
              }
          } else visibleEntries.forEach { (icon,title,subtitle) ->
              CatalogueCard(icon,title,subtitle) { onOpenDetails(title,subtitle) }
          }

          if(page=="Assessment") Button(onClick={
            onOpenDetails("Career Strengths Assessment","A short guided assessment to identify your interests and strengths.")
        },modifier=Modifier.fillMaxWidth().padding(top=11.dp),shape=RoundedCornerShape(11.dp)){
            Text("Start assessment")
        }

    }

}

@Composable private fun CatalogueCard(icon:String,title:String,subtitle:String,onClick:()->Unit) {
    Surface(Modifier.fillMaxWidth().padding(top=7.dp).clickable(onClick=onClick).testTag("catalogue-$title"),
        shape=RoundedCornerShape(12.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,Line)) {
        Row(Modifier.padding(11.dp),verticalAlignment=Alignment.CenterVertically) {
            FeatureIcon(icon)
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(title,color=Ink,fontSize=12.sp,fontWeight=FontWeight.Bold)
                Text(subtitle,color=Muted,fontSize=10.sp)
            }
        }
    }
}
