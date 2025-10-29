package com.ahanam05.galleon

//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import com.ahanam05.galleon.ui.theme.GalleonTheme
//import android.content.ContentValues.TAG
//import android.content.Context
//import android.credentials.Credential
//import android.credentials.GetCredentialException
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.Image
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.credentials.CredentialManager
//import androidx.credentials.exceptions.GetCredentialCancellationException
//import androidx.credentials.exceptions.GetCredentialCustomException
//import androidx.credentials.exceptions.NoCredentialException
//import androidx.credentials.GetCredentialRequest
//import com.google.android.libraries.identity.googleid.GetGoogleIdOption
//import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
//import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
//import java.security.SecureRandom
//import java.util.Base64
//import androidx.compose.runtime.LaunchedEffect
//import androidx.credentials.CustomCredential
//import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
//import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
//import com.google.firebase.Firebase
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.GoogleAuthProvider
//import com.google.firebase.auth.auth
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
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
import java.security.SecureRandom
import kotlin.io.encoding.Base64

//
//class MainActivity : ComponentActivity() {
//    private lateinit var auth: FirebaseAuth
//    auth = Firebase.auth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val webClientId = "566840063026-s3ecj1oa71n25nd8ug38mub3tkdobr2c.apps.googleusercontent.com"
//
//        enableEdgeToEdge()
//        setContent {
//            GalleonTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background,
//                ) {
//                    //BottomSheet(webClientId)
//
//                    //ButtonUI(webClientId)
//
//
//                }
//            }
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        updateUI(currentUser)
//    }
//}
//
//fun instantiate(webClientID: String){
//    val googleIdOption = GetGoogleIdOption.Builder()
//        // Your server's client ID, not your Android client ID.
//        .setServerClientId(webClientID)
//        // Only show accounts previously used to sign in.
//        .setFilterByAuthorizedAccounts(true)
//        .build()
//
//// Create the Credential Manager request
//    val request = GetCredentialRequest.Builder()
//        .addCredentialOption(googleIdOption)
//        .build()
//}
//
//private fun handleSignIn(credential: Credential) {
//    // Check if credential is of type Google ID
//    if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
//        // Create Google ID Token
//        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
//
//        // Sign in to Firebase with using the token
//        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
//    } else {
//        Log.w(TAG, "Credential is not of type Google ID!")
//    }
//}
//
//private fun firebaseAuthWithGoogle(idToken: String) {
//    val credential = GoogleAuthProvider.getCredential(idToken, null)
//    auth.signInWithCredential(credential)
//        .addOnCompleteListener(this) { task ->
//            if (task.isSuccessful) {
//                // Sign in success, update UI with the signed-in user's information
//                Log.d(TAG, "signInWithCredential:success")
//                val user = auth.currentUser
//                updateUI(user)
//            } else {
//                // If sign in fails, display a message to the user
//                Log.w(TAG, "signInWithCredential:failure", task.exception)
//                updateUI(null)
//            }
//        }
//}
//
//@Composable
//fun BottomSheet(webClientId: String) {
//    val context = LocalContext.current
//
//    LaunchedEffect(Unit) {
//        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
//            .setFilterByAuthorizedAccounts(true)
//            .setServerClientId(webClientId)
//            .setNonce(generateSecureRandomNonce())
//            .build()
//
//        val request: GetCredentialRequest = GetCredentialRequest.Builder()
//            .addCredentialOption(googleIdOption)
//            .build()
//
//        val e = signIn(request, context)
//        if (e is NoCredentialException) {
//            val googleIdOptionFalse: GetGoogleIdOption = GetGoogleIdOption.Builder()
//                .setFilterByAuthorizedAccounts(false)
//                .setServerClientId(webClientId)
//                .setNonce(generateSecureRandomNonce())
//                .build()
//
//            val requestFalse: GetCredentialRequest = GetCredentialRequest.Builder()
//                .addCredentialOption(googleIdOptionFalse)
//                .build()
//
//            signIn(requestFalse, context)
//        }
//    }
//}
//
//@Composable
//fun ButtonUI(webClientId: String) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    val onClick: () -> Unit = {
//        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
//            .Builder(serverClientId = webClientId)
//            .setNonce(generateSecureRandomNonce())
//            .build()
//
//        val request: GetCredentialRequest = GetCredentialRequest.Builder()
//            .addCredentialOption(signInWithGoogleOption)
//            .build()
//
//        coroutineScope.launch {
//            signIn(request, context)
//        }
//    }
//    Image(
//        painter = painterResource(id = R.drawable.siwg_button),
//        contentDescription = "",
//        modifier = Modifier
//            .fillMaxSize()
//            .clickable(enabled = true, onClick = onClick)
//    )
//}
//
//fun generateSecureRandomNonce(byteLength: Int = 32): String {
//    val randomBytes = ByteArray(byteLength)
//    SecureRandom.getInstanceStrong().nextBytes(randomBytes)
//    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
//}
//
//suspend fun signIn(request: GetCredentialRequest, context: Context): Exception? {
//    val credentialManager = CredentialManager.create(context)
//    val failureMessage = "Sign in failed!"
//    var e: Exception? = null
//    delay(250)
//    try {
//        val result = credentialManager.getCredential(
//            request = request,
//            context = context,
//        )
//        Log.i(TAG, result.toString())
//
//        Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
//        Log.i(TAG, "(☞ﾟヮﾟ)☞  Sign in Successful!  ☜(ﾟヮﾟ☜)")
//
//    } catch (e: GetCredentialException) {
//        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
//        Log.e(TAG, "$failureMessage: Failure getting credentials", e)
//
//    } catch (e: GoogleIdTokenParsingException) {
//        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
//        Log.e(TAG, "$failureMessage: Issue with parsing received GoogleIdToken", e)
//
//    } catch (e: NoCredentialException) {
//        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
//        Log.e(TAG, "$failureMessage: No credentials found", e)
//        return e
//
//    } catch (e: GetCredentialCustomException) {
//        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
//        Log.e(TAG, "$failureMessage: Issue with custom credential request", e)
//
//    } catch (e: GetCredentialCancellationException) {
//        Toast.makeText(context, ": Sign-in cancelled", Toast.LENGTH_SHORT).show()
//        Log.e(TAG, "$failureMessage: Sign-in was cancelled", e)
//    }
//    return null
//}

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    val webClientId = "566840063026-s3ecj1oa71n25nd8ug38mub3tkdobr2c.apps.googleusercontent.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        credentialManager = CredentialManager.create(baseContext)

        setContent {
            GalleonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background,
                ) {
                    ButtonUI()
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    @Composable
    fun ButtonUI() {
        Image(
            painter = painterResource(id = R.drawable.siwg_button),
            contentDescription = "Sign in with Google button",
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = true, onClick = { launchCredentialManager() })
        )
    }
    private fun launchCredentialManager() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            //.setNonce(generateSecureRandomNonce())
            .setFilterByAuthorizedAccounts(true)
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
                    val user = auth.currentUser
                    updateUI(user)
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

    private fun signOut() {
        auth.signOut()

        lifecycleScope.launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                updateUI(null)
            } catch (e: ClearCredentialException) {
                Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
    }

    companion object {
        private const val TAG = "GoogleActivity"
    }
}
