package com.elysium.orbithop.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.orbithop.R
import com.elysium.orbithop.core.auth.AuthRepository
import com.elysium.orbithop.core.i18n.Locales
import com.elysium.orbithop.core.store.LocalStore
import com.elysium.orbithop.core.util.Analytics
import com.elysium.orbithop.core.util.Feedback
import com.elysium.orbithop.ui.components.GhostButton
import com.elysium.orbithop.ui.components.OrbitButton
import com.elysium.orbithop.ui.theme.PlanetOrange
import com.elysium.orbithop.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Écran d'authentification : e-mail/mot de passe ou Google.
 * Le mode invité permet de jouer hors ligne immédiatement.
 */
@Composable
fun AuthScreen(onAuthenticated: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val webClientId = context.getString(R.string.google_web_client_id)

    var signUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101B3C))
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        Text(
            text = "ORBIT HOP",
            color = Color(0xFFF7F3E8),
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
        Text(
            text = Locales.t("auth_subtitle"),
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (signUpMode) {
            AuthField(value = name, label = Locales.t("name"), onChange = { name = it })
        }
        AuthField(
            value = email,
            label = Locales.t("email"),
            hint = Locales.t("email_hint"),
            onChange = { email = it }
        )
        AuthField(
            value = password,
            label = Locales.t("password"),
            hint = Locales.t("password_hint"),
            isPassword = true,
            onChange = { password = it }
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error!!, color = Color(0xFFFF8A65), fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        OrbitButton(
            text = if (signUpMode) Locales.t("sign_up") else Locales.t("sign_in"),
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy
        ) {
            busy = true
            error = null
            scope.launch {
                val result = if (signUpMode) {
                    AuthRepository.register(email, password, name)
                } else {
                    AuthRepository.signIn(email, password)
                }
                result
                    .onSuccess {
                        LocalStore.setFirstLaunchDone()
                        Analytics.setUserId(AuthRepository.currentUid)
                        Feedback.play(Feedback.Sound.REWARD)
                        onAuthenticated()
                    }
                    .onFailure { error = Locales.t("auth_error", it.message ?: "?") }
                busy = false
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        GhostButton(
            text = if (signUpMode) Locales.t("sign_in") else Locales.t("sign_up"),
            modifier = Modifier.fillMaxWidth()
        ) {
            signUpMode = !signUpMode
            error = null
        }

        Spacer(modifier = Modifier.height(18.dp))

        GhostButton(text = Locales.t("google_sign_in"), modifier = Modifier.fillMaxWidth(), enabled = !busy) {
            if (webClientId.isBlank()) {
                error = Locales.t("google_not_ready")
            } else if (activity != null) {
                busy = true
                error = null
                scope.launch {
                    AuthRepository.signInWithGoogle(activity, webClientId)
                        .onSuccess {
                            LocalStore.setFirstLaunchDone()
                            Analytics.setUserId(AuthRepository.currentUid)
                            onAuthenticated()
                        }
                        .onFailure {
                            error = if (it.message == "GOOGLE_NOT_CONFIGURED") {
                                Locales.t("google_not_ready")
                            } else {
                                Locales.t("auth_error", it.message ?: "?")
                            }
                        }
                    busy = false
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        GhostButton(text = Locales.t("guest"), modifier = Modifier.fillMaxWidth()) {
            AuthRepository.continueAsGuest()
            LocalStore.setFirstLaunchDone()
            Feedback.play(Feedback.Sound.CLICK)
            onAuthenticated()
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun AuthField(
    value: String,
    label: String,
    hint: String = "",
    isPassword: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = { Text(hint, color = TextSecondary.copy(alpha = 0.5f)) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PlanetOrange,
            unfocusedBorderColor = Color(0xFF2C4192),
            focusedLabelColor = PlanetOrange,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = Color(0xFFF7F3E8),
            unfocusedTextColor = Color(0xFFF7F3E8),
            cursorColor = PlanetOrange
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}
