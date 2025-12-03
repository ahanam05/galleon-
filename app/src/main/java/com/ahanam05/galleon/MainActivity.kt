package com.ahanam05.galleon

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ahanam05.galleon.ui.theme.GalleonTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        credentialManager = CredentialManager.create(baseContext)

        setContent {
            GalleonTheme {
                val startDestination = if (auth.currentUser != null) HOME_SCREEN else LANDING_SCREEN
                Surface(
                    modifier = Modifier.fillMaxSize(), color =  Color(0xFFFFF8E7) ,
                ) {
                    NavigationHandler(auth = auth,
                        startDestination = startDestination,
                        onSignInClick = { launchCredentialManager() },
                        onSignOutClick = { navController -> signOut(navController) })
                }
            }
        }
    }

    private fun launchCredentialManager() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(WEB_CLIENT_ID)
            //.setNonce(generateSecureRandomNonce())
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = baseContext,
                    request = request
                )

                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    updateUI(auth.currentUser)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

//    fun generateSecureRandomNonce(byteLength: Int = 32): String {
//    val randomBytes = ByteArray(byteLength)
//    SecureRandom.getInstanceStrong().nextBytes(randomBytes)
//    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
//}

    private fun signOut(navController: NavController) {
        auth.signOut()

        lifecycleScope.launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                //updateUI(null)
            } catch (e: ClearCredentialException) {
                Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Log.d(TAG, "User ${user.email} is signed in")
        } else {
            Log.d(TAG, "User is signed out")
        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val WEB_CLIENT_ID = "566840063026-6j5mep165kmru5oar9q29e6dh2gh3dbq.apps.googleusercontent.com"
        private const val HOME_SCREEN = "home"
        private const val LANDING_SCREEN = "landing"
    }

    @Composable
    fun NavigationHandler(
        auth: FirebaseAuth,
        startDestination: String,
        onSignInClick: () -> Unit,
        onSignOutClick: (NavController) -> Unit
    ){
        val navController = rememberNavController()
        var currentUser by remember { mutableStateOf(auth.currentUser) }

        DisposableEffect(auth) {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                currentUser = user
                if (user != null) {
                    navController.navigate(HOME_SCREEN) { popUpTo(0) }
                } else {
                    navController.navigate(LANDING_SCREEN) { popUpTo(0) }
                }
            }
            auth.addAuthStateListener(listener)

            onDispose {
                auth.removeAuthStateListener(listener)
            }
        }

        NavHost(navController = navController, startDestination = startDestination){
            composable(LANDING_SCREEN){
                LandingScreen(onSignInClick = onSignInClick)
            }
            composable(HOME_SCREEN){
                val user = currentUser
                if (user != null){
                    HomeScreen(user = user, onSignOutClick = { onSignOutClick(navController) })
                }

            }
        }

    }
}
