package com.lnxteam.atmoscope.viewmodel

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    val isLoggedIn: Boolean get() = auth.currentUser != null

    private val webClientId = "407616959295-r6p26cap22pjd83l62369m9eichqb5ns.apps.googleusercontent.com"

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email dan password tidak boleh kosong")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Login gagal")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(parseAuthError(e.message))
            }
        }
    }

    fun registerWithEmail(email: String, password: String, displayName: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email dan password tidak boleh kosong")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password minimal 6 karakter")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    if (displayName.isNotBlank()) {
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()
                        user.updateProfile(profileUpdates).await()
                    }
                    _currentUser.value = auth.currentUser
                    _authState.value = AuthState.Success(auth.currentUser!!)
                } else {
                    _authState.value = AuthState.Error("Registrasi gagal")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(parseAuthError(e.message))
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val credentialResponse = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val googleIdToken = GoogleIdTokenCredential
                    .createFrom(credentialResponse.credential.data)
                    .idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val result = auth.signInWithCredential(firebaseCredential).await()
                val user = result.user
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Google Sign-In gagal")
                }
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error("Google Sign-In dibatalkan")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(parseAuthError(e.message))
            }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun deleteAccount(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                auth.currentUser?.delete()?.await()
                auth.signOut()
                _currentUser.value = null
                _authState.value = AuthState.Idle
                onDone()
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Gagal menghapus akun: ${e.message}")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun parseAuthError(message: String?): String = when {
        message == null -> "Terjadi kesalahan"
        "email address is already in use" in message -> "Email sudah terdaftar"
        "no user record" in message || "password is invalid" in message -> "Email atau password salah"
        "badly formatted" in message -> "Format email tidak valid"
        "network error" in message -> "Periksa koneksi internet"
        else -> "Terjadi kesalahan, coba lagi"
    }
}
