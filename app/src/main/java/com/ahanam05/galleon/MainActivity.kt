package com.ahanam05.galleon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ahanam05.galleon.ui.theme.GalleonTheme
import android.content.ContentValues.TAG
import android.content.Context
import android.credentials.GetCredentialException
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.SecureRandom
import java.util.Base64
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webClientId = "566840063026-s3ecj1oa71n25nd8ug38mub3tkdobr2c.apps.googleusercontent.com"
        enableEdgeToEdge()
        setContent {
            GalleonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background,
                ) {
                    BottomSheet(webClientId)

                    ButtonUI(webClientId)
                }
            }
        }
    }
}

@Composable
fun BottomSheet(webClientId: String) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val e = signIn(request, context)
        if (e is NoCredentialException) {
            val googleIdOptionFalse: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(generateSecureRandomNonce())
                .build()

            val requestFalse: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOptionFalse)
                .build()

            signIn(requestFalse, context)
        }
    }
}

@Composable
fun ButtonUI(webClientId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val onClick: () -> Unit = {
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        coroutineScope.launch {
            signIn(request, context)
        }
    }
    Image(
        painter = painterResource(id = R.drawable.siwg_button),
        contentDescription = "",
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = true, onClick = onClick)
    )
}

fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom.getInstanceStrong().nextBytes(randomBytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
}

suspend fun signIn(request: GetCredentialRequest, context: Context): Exception? {
    val credentialManager = CredentialManager.create(context)
    val failureMessage = "Sign in failed!"
    var e: Exception? = null
    delay(250)
    try {
        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )
        Log.i(TAG, result.toString())

        Toast.makeText(context, "Sign in successful!", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "(☞ﾟヮﾟ)☞  Sign in Successful!  ☜(ﾟヮﾟ☜)")

    } catch (e: GetCredentialException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: Failure getting credentials", e)

    } catch (e: GoogleIdTokenParsingException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: Issue with parsing received GoogleIdToken", e)

    } catch (e: NoCredentialException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: No credentials found", e)
        return e

    } catch (e: GetCredentialCustomException) {
        Toast.makeText(context, failureMessage, Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: Issue with custom credential request", e)

    } catch (e: GetCredentialCancellationException) {
        Toast.makeText(context, ": Sign-in cancelled", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "$failureMessage: Sign-in was cancelled", e)
    }
    return null
}