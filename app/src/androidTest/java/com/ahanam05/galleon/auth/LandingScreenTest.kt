package com.ahanam05.galleon.auth

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ahanam05.galleon.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LandingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        navController = TestNavHostController(InstrumentationRegistry.getInstrumentation().targetContext)
        navController.navigatorProvider.addNavigator(ComposeNavigator())
    }

    @Test
    fun galleonLogo_isVisible() {
        composeTestRule.setContent {
            LandingScreen(onSignInClick = { })
        }

        val logoContentDesc = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.galleon_logo_desc)
        composeTestRule.onNodeWithContentDescription(logoContentDesc).assertIsDisplayed()
    }

    @Test
    fun galleonTagline_isVisible() {
        composeTestRule.setContent {
            LandingScreen(onSignInClick = { })
        }

        val taglineText = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.landing_screen_tagline)
        composeTestRule.onNodeWithText(taglineText).assertIsDisplayed()
    }

    @Test
    fun signInWithGoogleButton_isClickable() {
        composeTestRule.setContent {
            LandingScreen(onSignInClick = { })
        }

        val signInButtonDesc = InstrumentationRegistry.getInstrumentation().targetContext.getString(
            R.string.sign_in_button_desc)
        composeTestRule.onNodeWithContentDescription(signInButtonDesc).assertHasClickAction()
    }
}
