package com.pathfinder.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pathfinder.app.data.local.defaultOpportunities
import com.pathfinder.app.data.local.defaultUniversityProgrammes
import com.pathfinder.app.ui.screens.careers.CareerDetailsScreen
import com.pathfinder.app.ui.screens.categories.CategoryScreen
import com.pathfinder.app.ui.theme.PathwiseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UniversityProgrammeNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun universityListOpensItsOwnProfileAndOnlyItsVerifiedProgrammeDetails() {
        val institutions = defaultOpportunities().filter {
            it.category == "Universities" && (it.id == "uni-malawi" || it.id.startsWith("uni-expanded-"))
        }
        val programmes = defaultUniversityProgrammes()
        compose.setContent {
            PathwiseTheme {
                var selected by remember { mutableStateOf<Triple<String,String,String>?>(null) }
                if (selected == null) {
                    CategoryScreen(PaddingValues(0.dp),"Universities",{}, {}, { title, subtitle ->
                        selected = Triple(title,subtitle,"Universities")
                    },{},institutions)
                } else {
                    val detail = selected!!
                    CareerDetailsScreen(PaddingValues(0.dp),detail.first,detail.second,detail.third,emptySet(),{}, {}, {}, {},institutions,programmes)
                }
            }
        }

        compose.onNodeWithTag("catalogue-University of Malawi").performScrollTo().performClick()
        compose.onNodeWithText("Programmes Offered").assertExists()
        compose.onNodeWithTag("university-programme-programme-unima-bsc-computer-science").assertExists()
        compose.onNodeWithText("Computer Science and Engineering (Course 6-3)").assertDoesNotExist()
        compose.onNodeWithTag("university-programme-programme-unima-bsc-computer-science").performScrollTo().performClick()
        compose.onNodeWithText("Bachelor of Science (BSc)").assertExists()
        compose.onNodeWithText("4 years").assertExists()
        compose.onNodeWithText("Required school subjects or qualifications").assertExists()
    }
}
