package com.ahanam05.galleon.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahanam05.galleon.R
import com.google.firebase.auth.FirebaseUser
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun mockFirebaseUser(displayName: String): FirebaseUser {
        val mocked = Mockito.mock(FirebaseUser::class.java)
        Mockito.`when`(mocked.displayName).thenReturn(displayName)
        return mocked
    }

    @Test
    fun topBar_isVisible() {
        val user = mockFirebaseUser("Test User")
        composeTestRule.setContent {
            HomeScreen(onSignOutClick = {}, user = user)
        }

        composeTestRule.onNodeWithText("Welcome, Test User")
            .assertIsDisplayed()
    }

    @Test
    fun timePeriodTabs_areVisibleAndClickable() {
        val user = mockFirebaseUser("Test User")
        composeTestRule.setContent {
            HomeScreen(onSignOutClick = {}, user = user)
        }
        val daily = composeTestRule.onNodeWithText(Modes.DAILY)
        val weekly = composeTestRule.onNodeWithText(Modes.WEEKLY)
        val monthly = composeTestRule.onNodeWithText(Modes.MONTHLY)

        daily.assertIsDisplayed()
        weekly.assertIsDisplayed()
        monthly.assertIsDisplayed()
        daily.performClick()
        weekly.performClick()
        monthly.performClick()
    }

    @Test
    fun floatingAddButton_opensExpenseModal() {
        val user = mockFirebaseUser("Test User")
        composeTestRule.setContent {
            HomeScreen(onSignOutClick = {}, user = user)
        }
        val addFabDesc = composeTestRule.activity.getString(R.string.add_desc)
        val addExpenseTitle = composeTestRule.activity.getString(R.string.add_expense_text)
        val fabNode = composeTestRule.onNodeWithContentDescription(addFabDesc)

        fabNode.assertIsDisplayed()
        fabNode.performClick()

        composeTestRule.onNodeWithText(addExpenseTitle)
            .assertIsDisplayed()
    }

    @Test
    fun clickingProfile_opensNavigationDrawer() {
        val user = mockFirebaseUser("Test User")
        composeTestRule.setContent {
            HomeScreen(onSignOutClick = {}, user = user)
        }
        val profileImgDesc = composeTestRule.activity.getString(R.string.profile_img_desc)
        val profileNode = composeTestRule.onNodeWithTag(profileImgDesc)

        profileNode.assertIsDisplayed()
        profileNode.performClick()

        val signOutText = composeTestRule.activity.getString(R.string.sign_out_text)
        composeTestRule.onNodeWithText(signOutText)
            .assertIsDisplayed()
    }

    @Test
    fun signOut_goesToLanding() {
        val user = mockFirebaseUser("Test User")
        val onSignOutClicked: () -> Unit = Mockito.mock()
        composeTestRule.setContent {
            HomeScreen(onSignOutClick = onSignOutClicked, user = user)
        }
        val profileImgDesc = composeTestRule.activity.getString(R.string.profile_img_desc)
        val profileNode = composeTestRule.onNodeWithTag(profileImgDesc)
        profileNode.performClick()

        val signOutText = composeTestRule.activity.getString(R.string.sign_out_text)
        val signOutNode = composeTestRule.onNodeWithText(signOutText)
        signOutNode.performClick()

        Mockito.verify(onSignOutClicked).invoke()
    }

    @Test
    fun editExpenseCard_opensEditExpenseModal() {
        val user = mockFirebaseUser("Test User")
        composeTestRule.setContent {
            HomeScreen(onSignOutClick = {}, user = user)
        }
        val editDesc = composeTestRule.activity.getString(R.string.edit_desc)
        val editNodes = composeTestRule.onAllNodesWithContentDescription(editDesc)

        editNodes[0].performClick()

        val editExpenseTitle = composeTestRule.activity.getString(R.string.edit_expense_text)
        composeTestRule.onNodeWithText(editExpenseTitle).assertIsDisplayed()
    }
}
