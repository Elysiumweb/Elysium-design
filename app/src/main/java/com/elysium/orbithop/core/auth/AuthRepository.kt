package com.elysium.orbithop.core.auth

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.elysium.orbithop.core.store.FirestoreRepository
import com.elysium.orbithop.core.store.LocalStore
import com.elysium.orbithop.core.util.await
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest

/** État de session : invité (hors ligne) ou connecté. */
sealed interface AuthState {
    data object Guest : AuthState
    data class SignedIn(val uid: String, val name: String, val email: String) : AuthState
    data object SignedOut : AuthState
}

/**
 * Authentification Firebase : e-mail/mot de passe ou Google (Credential Manager).
 * Le mode invité reste jouable hors ligne (les scores locaux sont conservés).
 */
object AuthRepository {

    var state by mutableStateOf<AuthState>(AuthState.SignedOut)
        private set

    private var registered = false

    fun init() {
        if (registered) return
        registered = true
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            state = if (user != null) {
                AuthState.SignedIn(
                    uid = user.uid,
                    name = user.displayName ?: user.email?.substringBefore("@") ?: "Pilote",
                    email = user.email ?: ""
                )
            } else if (state is AuthState.Guest) {
                AuthState.Guest
            } else {
                AuthState.SignedOut
            }
        }
    }

    fun continueAsGuest() {
        state = AuthState.Guest
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        state = AuthState.SignedOut
    }

    val currentUid: String?
        get() = (state as? AuthState.SignedIn)?.uid

    suspend fun register(email: String, password: String, name: String): Result<AuthState> = runCatching {
        require(email.isNotBlank() && "@" in email) { "E-mail invalide" }
        require(password.length >= 8) { "Mot de passe : 8 caractères minimum" }
        require(name.isNotBlank()) { "Nom requis" }
        val result = FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email.trim(), password)
            .await()
        val user = result.user ?: error("Création du compte impossible")
        user.updateProfile(userProfileChangeRequest { displayName = name.trim() }).await()
        LocalStore.setGuestName(name.trim())
        val newState = AuthState.SignedIn(user.uid, name.trim(), email.trim())
        state = newState
        FirestoreRepository.onSignedIn(user.uid, name.trim(), email.trim())
        newState
    }

    suspend fun signIn(email: String, password: String): Result<AuthState> = runCatching {
        val result = FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email.trim(), password)
            .await()
        val user = result.user ?: error("Connexion impossible")
        val newState = AuthState.SignedIn(
            user.uid,
            user.displayName ?: user.email?.substringBefore("@") ?: "Pilote",
            user.email ?: ""
        )
        state = newState
        FirestoreRepository.onSignedIn(user.uid, newState.name, newState.email)
        newState
    }

    /**
     * Connexion Google via Credential Manager (l'ancienne API GoogleSignIn est dépréciée).
     * Nécessite le Web client ID dans res/values/strings.xml (google_web_client_id).
     */
    suspend fun signInWithGoogle(activity: Activity, webClientId: String): Result<AuthState> = runCatching {
        require(webClientId.isNotBlank()) { "GOOGLE_NOT_CONFIGURED" }
        val credentialManager = CredentialManager.create(activity)
        val googleOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()
        val response = credentialManager.getCredential(activity, request)
        val googleId = GoogleIdTokenCredential.createFrom(response.credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleId.idToken, null)
        val result = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
        val user = result.user ?: error("Connexion Google impossible")
        val name = user.displayName ?: googleId.displayName ?: "Pilote"
        val newState = AuthState.SignedIn(user.uid, name, user.email ?: "")
        state = newState
        FirestoreRepository.onSignedIn(user.uid, name, newState.email)
        newState
    }
}
