package com.example.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.repository.StudyRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: FirebaseUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class AuthViewModel(
    private val studyRepository: StudyRepository? = null,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _uiState = MutableStateFlow<AuthUiState>(
        auth.currentUser?.let { AuthUiState.Authenticated(it) } ?: AuthUiState.Idle
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val credentialManager = CredentialManager.create(context)
                val serverClientId = context.getString(R.string.default_web_client_id)

                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user

                    if (user != null) {
                        val isSuperAdmin = (user.email ?: "").equals(com.example.data.model.SUPER_ADMIN_EMAIL, ignoreCase = true)
                        try {
                            val existingProfile = studyRepository?.getUserProfile(user.uid)
                            if (existingProfile == null) {
                                val newProfile = UserProfile(
                                    id = user.uid,
                                    userId = user.uid,
                                    displayName = user.displayName ?: "Student",
                                    email = user.email ?: "",
                                    status = if (isSuperAdmin) "approved" else "pending",
                                    role = if (isSuperAdmin) "super_admin" else "student",
                                    activeFolderId = "study_materials_root",
                                    activeFolderName = "بوابة البرمجة للبكالوريا",
                                    allowedFolderIds = emptyList()
                                )
                                studyRepository?.saveUserProfile(newProfile)
                            }
                        } catch (profileError: Exception) {
                            Log.e(TAG, "Failed to load or create the user profile", profileError)
                            auth.signOut()
                            _uiState.value = AuthUiState.Error("Could not load your account profile. Please try signing in again.")
                            return@launch
                        }
                        _uiState.value = AuthUiState.Authenticated(user)
                    } else {
                        _uiState.value = AuthUiState.Error("Failed to obtain Firebase user.")
                    }
                } else {
                    _uiState.value = AuthUiState.Error("Unexpected credential format received.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In was cancelled by user: ${e.message}")
                _uiState.value = AuthUiState.Idle
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google Sign-In credential error: ${e.message}", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Google Sign-In failed.")
            } catch (e: Exception) {
                Log.e(TAG, "Authentication exception: ${e.message}", e)
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "An unexpected error occurred during sign-in.")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
