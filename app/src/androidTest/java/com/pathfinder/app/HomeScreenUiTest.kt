package com.pathfinder.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pathfinder.app.ui.screens.home.HomeScreen
import com.pathfinder.app.ui.theme.PathwiseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenUiTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun homeShowsCareerAndOpportunitySections() {
        composeRule.setContent {
            PathwiseTheme {
                HomeScreen(PaddingValues(), {}, {}, {}, emptySet(), {}, {}, { _, _, _ -> }, {}, {})
            }
        }
        composeRule.onNodeWithText("Where do you want to go?").assertExists()
        composeRule.onNodeWithText("Careers", substring = false).assertExists()
        composeRule.onNodeWithText("Scholarships", substring = false).assertExists()
        composeRule.onNodeWithText("Your top career match").assertExists()
    }

    @Test fun seeAllOpensExploreAndCareerCardOpensItsDetails() {
        var exploreOpened = false
        var openedCareer = ""
        composeRule.setContent {
            PathwiseTheme {
                HomeScreen(PaddingValues(), {}, {}, {}, emptySet(), {}, {}, { title, _, _ -> openedCareer = title }, {}, { exploreOpened = true })
            }
        }
        composeRule.onNodeWithText("See all ›").performClick()
        composeRule.onNodeWithText("UX Designer").performClick()
        composeRule.runOnIdle {
            assertTrue(exploreOpened)
            assertEquals("UX Designer", openedCareer)
        }
    }
}
