package com.elysium.orbithop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.elysium.orbithop.billing.BillingManager
import com.elysium.orbithop.core.auth.AuthRepository
import com.elysium.orbithop.core.store.LocalStore
import com.elysium.orbithop.core.util.Analytics
import com.elysium.orbithop.core.util.Feedback
import com.elysium.orbithop.ui.screens.AuthScreen
import com.elysium.orbithop.ui.screens.GameScreen
import com.elysium.orbithop.ui.screens.LeaderboardScreen
import com.elysium.orbithop.ui.screens.MenuScreen
import com.elysium.orbithop.ui.screens.SettingsScreen
import com.elysium.orbithop.ui.screens.ShopScreen
import com.elysium.orbithop.ui.theme.OrbitHopTheme

private enum class Screen { AUTH, MENU, GAME, SHOP, LEADERBOARD, SETTINGS }

/**
 * Racine de l'application : navigation simple par état (pas de NavComponent —
 * suffisant et robuste pour un jeu à écrans successifs).
 */
@Composable
fun AppRoot() {
    val context = LocalContext.current
    var screen by remember { mutableStateOf<Screen?>(null) } // null = splash
    var gameKey by remember { mutableIntStateOf(0) }

    // Décision de l'écran initial + synchro de session
    LaunchedEffect(Unit) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        when {
            user != null -> {
                AuthRepository.init()
                Analytics.setUserId(user.uid)
                screen = Screen.MENU
            }
            LocalStore.firstLaunchDone -> {
                AuthRepository.continueAsGuest()
                screen = Screen.MENU
            }
            else -> screen = Screen.AUTH
        }
        Feedback.startMusic()
        BillingManager.init(context)
    }

    OrbitHopTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101B3C))
        ) {
            when (screen) {
                null -> Unit // Splash minimal (fond déjà peint)
                Screen.AUTH -> AuthScreen(
                    onAuthenticated = { screen = Screen.MENU }
                )

                Screen.MENU -> MenuScreen(
                    onPlay = {
                        gameKey++
                        screen = Screen.GAME
                    },
                    onShop = { screen = Screen.SHOP },
                    onLeaderboard = { screen = Screen.LEADERBOARD },
                    onSettings = { screen = Screen.SETTINGS },
                    onSignOut = {
                        AuthRepository.signOut()
                        screen = Screen.AUTH
                    }
                )

                Screen.GAME -> key(gameKey) {
                    GameScreen(
                        onExitToMenu = { screen = Screen.MENU },
                        onRestart = { gameKey++ }
                    )
                }

                Screen.SHOP -> ShopScreen(onBack = { screen = Screen.MENU })
                Screen.LEADERBOARD -> LeaderboardScreen(onBack = { screen = Screen.MENU })
                Screen.SETTINGS -> SettingsScreen(onBack = { screen = Screen.MENU })
            }
        }
    }
}
