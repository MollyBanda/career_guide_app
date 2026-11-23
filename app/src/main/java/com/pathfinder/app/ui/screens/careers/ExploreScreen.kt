package com.pathfinder.app.ui.screens.careers

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

@Composable
fun ExploreScreen(
    pad: PaddingValues,
    notify: (String) -> Unit,
    onOpenDetails: (String, String, String) -> Unit,
    onProfile: () -> Unit,
    catalogItems: List<OpportunityEntity>,
    savedTitles: Set<String> = emptySet(),
    onToggleSave: (String) -> Unit = {},
    openCategory: ((String) -> Unit)? = null
) {
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Matches") }

    val careers = remember(catalogItems) {
        catalogItems.filter { item ->
            item.category != "Assessment" && (item.category != "Universities" || item.id == "uni-malawi" || item.id.startsWith("uni-expanded-"))
        }.map { item ->
            val category = when {
                item.category == "Scholarships" || item.title.contains("Scholarship", ignoreCase = true) -> "Scholarships"
                item.category == "Internships" || item.title.contains("Intern", ignoreCase = true) -> "Internships"
                item.category == "Universities" || item.title.contains("University", ignoreCase = true) -> "Universities"
                item.category == "Training" || item.title.contains("Training", ignoreCase = true) || item.title.contains("Analytics", ignoreCase = true) -> "Training"
                else -> item.category
            }
            val icon = iconName(item.category, item.title)
            Triple(icon, item.title, item.subtitle to category)
        }
    }

    val visibleCareers = remember(careers, query, selectedFilter) {
        val q = query.trim()
        careers.filter { (_, title, subAndCat) ->
            val subtitle = subAndCat.first
            val category = subAndCat.second
            val matchesQuery = q.isEmpty() ||
                    title.contains(q, ignoreCase = true) ||
                    subtitle.contains(q, ignoreCase = true) ||
                    category.contains(q, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "All Matches", "All" -> true
                "Careers" -> category == "Careers"
                "Universities" -> category == "Universities"
                "Scholarships" -> category == "Scholarships"
                "Training" -> category == "Training"
                "Internships" -> category == "Internships"
                "Tech & Design" -> subtitle.contains("Technology", ignoreCase = true) ||
                        subtitle.contains("Design", ignoreCase = true) ||
                        title.contains("Software", ignoreCase = true) ||
                        title.contains("UX", ignoreCase = true) ||
                        title.contains("Data", ignoreCase = true) ||
                        title.contains("Cyber", ignoreCase = true)
                "Science" -> subtitle.contains("Science", ignoreCase = true) ||
                        title.contains("Science", ignoreCase = true) ||
                        title.contains("Scientist", ignoreCase = true) ||
                        title.contains("Health", ignoreCase = true) ||
                        title.contains("Environmental", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(pad)
            .verticalScroll(rememberScrollState())
            .background(Canvas)
            .padding(horizontal = 16.dp, vertical = 11.dp)
    ) {
        DashboardHeader("Explore", onProfile = onProfile)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Search careers and opportunities", fontSize = 12.sp, color = Muted) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search careers", tint = Muted) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Text("×", fontSize = 20.sp, color = Muted)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = Line,
                focusedBorderColor = Blue
            )
        )
        Spacer(Modifier.height(13.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TOP MATCHES", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${visibleCareers.size} results", color = Muted, fontSize = 10.sp)
        }

        Text("Careers to explore", Modifier.padding(top = 4.dp, bottom = 9.dp), color = Ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All Matches", "Careers", "Universities", "Scholarships", "Training", "Internships", "Tech & Design", "Science").forEach { filter ->
                FilterPill(filter, selectedFilter == filter) {
                    selectedFilter = filter
                }
            }
        }

        Spacer(Modifier.height(9.dp))
        if (visibleCareers.isEmpty()) {
            Text("No careers match that search yet.", Modifier.padding(vertical = 18.dp), color = Muted, fontSize = 12.sp)
        }
        visibleCareers.forEach { (icon, title, subAndCat) ->
            val subtitle = subAndCat.first
            val category = subAndCat.second
            val isSaved = title in savedTitles
            ExploreCareerCard(
                icon = icon,
                title = title,
                subtitle = subtitle,
                isSaved = isSaved,
                onToggleSave = { onToggleSave(title) },
                onClick = { onOpenDetails(title, subtitle, category) }
            )
        }
    }
}

@Composable
private fun ExploreCareerCard(
    icon: String,
    title: String,
    subtitle: String,
    isSaved: Boolean = false,
    onToggleSave: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 7.dp)
            .clickable(onClickLabel = "View $title", onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Line)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeatureIcon(icon)
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Ink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp
                )
                Text(
                    text = subtitle,
                    color = Muted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (onToggleSave != null) {
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (isSaved) "Remove $title from saved" else "Save $title",
                        tint = Blue,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}
