package com.pathfinder.app.ui.screens.saved

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


@Composable fun SavedScreen(
    pad: PaddingValues,
    onBack: () -> Unit,
    explore: () -> Unit,
    savedTitles: Set<String>,
    onToggleSave: (String) -> Unit,
    onOpenDetails: (String, String, String) -> Unit,
    onProfile: () -> Unit,
    catalogItems: List<OpportunityEntity>
) {
    // Show only items the user saved, across every catalogue category.
    val savedItems = catalogItems.filter { it.title in savedTitles }
    Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).background(Canvas).padding(horizontal = 16.dp, vertical = 11.dp)) {
        DashboardHeader("Saved", onBack = onBack, onProfile = onProfile)
        Spacer(Modifier.height(17.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Keep your possibilities", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("in one place.", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Pill("All Saved " + savedItems.size, active = true)
            Pill("Scholarships " + savedItems.count { it.category == "Scholarships" })
            Pill("Careers " + savedItems.count { it.category == "Careers" })
        }
        if (savedItems.isEmpty()) {
            Text("Nothing saved yet. Bookmark an item to keep it here.", Modifier.padding(vertical = 14.dp), color = Muted, fontSize = 12.sp)
        } else {
            Spacer(Modifier.height(11.dp))
            Text("SAVED ITEMS", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            savedItems.forEach { item ->
                SavedCard(iconName(item.category, item.title), item.title, item.subtitle, item.overview,
                    item.category, item.provider.ifBlank { item.duration.ifBlank { item.deadline.ifBlank { "Saved" } } }, true, onToggleSave) {
                    onOpenDetails(item.title, item.subtitle, item.category)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.fillMaxWidth().padding(11.dp)) {
                Text("RECOMMENDATIONS", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Looking for more?", Modifier.padding(top = 4.dp), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Save careers, scholarships, and other opportunities to revisit their details here.", Modifier.padding(top = 3.dp), color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(savedItems.size.toString() + " saved items", color = Muted, fontSize = 10.sp)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = explore, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                        Text("Explore more", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
@Composable private fun SavedCard(icon:String,title:String,subtitle:String,detail:String,tag1:String,tag2:String,
    isSaved:Boolean,onToggleSave:(String)->Unit,openDetails:()->Unit){

     Surface(Modifier.fillMaxWidth().padding(top=6.dp).clickable(onClick=openDetails),shape=RoundedCornerShape(13.dp),
        color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,Line)){
        Column(Modifier.padding(10.dp)){

                 Row(verticalAlignment=Alignment.CenterVertically){
                FeatureIcon(icon)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)){
                    Text(title,color=Ink,fontSize=12.sp,fontWeight=FontWeight.Bold)
                    Text(subtitle,color=Muted,fontSize=10.sp)
                }
                IconButton(onClick={
                    onToggleSave(title)
                },modifier=Modifier.size(34.dp)){
                    Icon(if(isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,contentDescription=if(isSaved) "Remove $title from saved" else "Save $title",
                        tint=Blue,modifier=Modifier.size(20.dp))
                }
            }

              Text(detail,Modifier.padding(top=8.dp),color=Muted,fontSize=10.sp)
            Row(Modifier.padding(top=6.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                Pill(tag1)
                Pill(tag2)
            }

        }
    }

}
