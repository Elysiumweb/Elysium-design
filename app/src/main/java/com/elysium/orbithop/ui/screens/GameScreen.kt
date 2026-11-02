package com.elysium.orbithop.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.orbithop.ads.AdManager
import com.elysium.orbithop.core.auth.AuthRepository
import com.elysium.orbithop.core.auth.AuthState
import com.elysium.orbithop.core.i18n.Locales
import com.elysium.orbithop.core.model.GameResult
import com.elysium.orbithop.core.model.ScoreEntry
import com.elysium.orbithop.core.store.FirestoreRepository
import com.elysium.orbithop.core.store.LocalStore
import com.elysium.orbithop.core.util.Analytics
import com.elysium.orbithop.core.util.Feedback
import com.elysium.orbithop.game.GameCanvas
import com.elysium.orbithop.game.GameEngine
import com.elysium.orbithop.ui.components.GhostButton
import com.elysium.orbithop.ui.components.OrbitButton
import com.elysium.orbithop.ui.theme.CosmicNavyDeep
import com.elysium.orbithop.ui.theme.PlanetOrange
import com.elysium.orbithop.ui.theme.StarYellow
import com.elysium.orbithop.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Écran de jeu : canvas plein écran + HUD + pause + tutoriel + fin de partie.
 */
@Composable
fun GameScreen(
    onExitToMenu: () -> Unit,
    onRestart: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val engine = remember { GameEngine() }
    var frameTick by remember { mutableFloatStateOf(0f) }
    var pauseVisible by remember { mutableStateOf(false) }
    var gameOverVisible by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<GameResult?>(null) }
    var continueAvailable by remember { mutableStateOf(false) }
    var doubleAvailable by remember { mutableStateOf(false) }
    var interstitialRunning by remember { mutableStateOf(false) }
    var systemFlash by remember { mutableIntStateOf(-1) }
    var hintIndex by remember { mutableIntStateOf(0) }
    var finalized by remember { mutableStateOf(false) }
    val showTutorial = !LocalStore.tutorialDone

    /** Enregistre définitivement la partie (local + Firestore) — une seule fois. */
    fun finalizeResult() {
        val res = result ?: return
        if (finalized) return
        finalized = true
        LocalStore.recordGameEnd(res.score, res.starsCollected)
        val uid = AuthRepository.currentUid
        if (uid != null) {
            val name = when (val s = AuthRepository.state) {
                is AuthState.SignedIn -> s.name
                else -> LocalStore.guestName
            }
            scope.launch {
                FirestoreRepository.submitScore(
                    uid,
                    ScoreEntry(
                        uid = uid,
                        name = name,
                        score = res.score,
                        stars = res.starsCollected,
                        skin = LocalStore.equippedSkin
                    )
                )
                FirestoreRepository.pushProfile(uid)
            }
        }
    }

    // ---- Boucle de jeu (60 fps via withFrameNanos) ----
    LaunchedEffect(Unit) {
        engine.start()
        Feedback.startMusic()
        Analytics.log("game_start")
        var last = withFrameNanos { it }
        var crashDelay = -1f
        var lastSystem = 0

        while (isActive) {
            val now = withFrameNanos { it }
            val dt = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
            last = now

            if (!interstitialRunning && !pauseVisible) {
                engine.update(dt)
            }

            // Flash « SYSTÈME N »
            if (engine.systemIndex != lastSystem) {
                lastSystem = engine.systemIndex
                systemFlash = lastSystem
                Feedback.play(Feedback.Sound.REWARD)
            }

            // Tutoriel progressif (< 1 minute)
            if (showTutorial && !gameOverVisible) {
                hintIndex = when {
                    engine.time < 5f -> 0
                    engine.time < 10f -> 1
                    else -> 2
                }
            }

            // Fin de partie : explosion puis panneau
            if (engine.crashed && crashDelay < 0f && !gameOverVisible) {
                crashDelay = 0f
                Feedback.play(Feedback.Sound.CRASH)
                Feedback.vibrate(220, 200)
            }
            if (crashDelay >= 0f && !gameOverVisible) {
                crashDelay += dt
                if (crashDelay >= 0.9f) {
                    crashDelay = -1f
                    val previousBest = LocalStore.bestScore
                    val res = engine.result(previousBest)
                    result = res
                    gameOverVisible = true
                    continueAvailable = !engine.continueUsed
                    doubleAvailable = res.starsCollected > 0
                    engine.finishGameOver()

                    if (showTutorial) LocalStore.markTutorialDone()

                    // L'enregistrement (local + Firestore) a lieu à la sortie définitive
                    // de l'écran de fin (REJOUER / MENU) pour éviter le double comptage
                    // si le joueur utilise « Continuer ».
                    finalized = false

                    Analytics.log("game_end") {
                        putInt("score", res.score)
                        putInt("stars", res.starsCollected)
                        putInt("system", engine.systemIndex)
                    }

                    // Interstitiel à l'écran de fin de partie (F07, plafonné)
                    if (activity != null && AdManager.shouldShowInterstitial()) {
                        interstitialRunning = true
                        AdManager.showInterstitialIfAllowed(activity) {
                            interstitialRunning = false
                        }
                    }
                }
            }

            frameTick += 1f
        }
    }

    // Efface le flash système après 2 s
    LaunchedEffect(systemFlash) {
        if (systemFlash >= 0) {
            delay(2000)
            systemFlash = -1
        }
    }

    // ---- Rendu ----
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(gameOverVisible, pauseVisible) {
                detectTapGestures {
                    if (!gameOverVisible && !pauseVisible && !interstitialRunning) {
                        engine.tap()
                        Feedback.play(Feedback.Sound.HOP)
                        Feedback.vibrate(18, 90)
                    }
                }
            }
    ) {
        GameCanvas(
            engine = engine,
            frameTick = frameTick,
            skinId = LocalStore.equippedSkin,
            modifier = Modifier.fillMaxSize()
        )

        // ---------- HUD ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GhostButton(text = "⏸ ${Locales.t("pause")}") {
                if (!gameOverVisible) {
                    engine.pause()
                    pauseVisible = true
                    Feedback.play(Feedback.Sound.CLICK)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${result?.score ?: (engine.scoreFloat.toInt() + engine.starsCollected * GameEngine.SCORE_PER_STAR)}",
                    color = Color(0xFFF7F3E8),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = Locales.t("score"),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "★", color = StarYellow, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(
                    text = "${engine.starsCollected}",
                    color = StarYellow,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Flash système
        if (systemFlash >= 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CosmicNavyDeep.copy(alpha = 0.85f))
                    .padding(horizontal = 26.dp, vertical = 14.dp)
            ) {
                Text(
                    text = Locales.t("system", systemFlash + 1),
                    color = PlanetOrange,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
            }
        }

        // Tutoriel
        if (showTutorial && !gameOverVisible && !pauseVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val hint = when (hintIndex) {
                    0 -> Locales.t("tap_to_hop")
                    1 -> Locales.t("collect_stars")
                    else -> Locales.t("avoid_debris")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(CosmicNavyDeep.copy(alpha = 0.85f))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(text = hint, color = Color(0xFFF7F3E8), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                GhostButton(text = Locales.t("tutorial_skip")) {
                    LocalStore.markTutorialDone()
                }
            }
        }

        // ---------- Pause ----------
        if (pauseVisible) {
            Overlay()
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = Locales.t("pause"),
                    color = Color(0xFFF7F3E8),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                OrbitButton(text = Locales.t("resume"), background = PlanetOrange) {
                    pauseVisible = false
                    engine.resume()
                }
                GhostButton(text = Locales.t("menu")) {
                    Feedback.pauseMusic()
                    onExitToMenu()
                }
            }
        }

        // ---------- Fin de partie ----------
        if (gameOverVisible && result != null) {
            Overlay()
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val res = result!!
                Text(
                    text = Locales.t("game_over"),
                    color = Color(0xFFF7F3E8),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                if (res.newBest) {
                    Text(
                        text = Locales.t("new_best"),
                        color = StarYellow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(text = "${res.score}", color = PlanetOrange, fontSize = 64.sp, fontWeight = FontWeight.Black)
                Text(
                    text = "${Locales.t("distance")} : ${res.distanceScore}  ·  ★ ${res.starsCollected}",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(22.dp))

                if (continueAvailable) {
                    OrbitButton(
                        text = Locales.t("continue_ad"),
                        background = PlanetOrange,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (activity != null) {
                            AdManager.showRewarded(activity, "continue") { earned ->
                                if (earned && engine.continueAfterReward()) {
                                    continueAvailable = false
                                    doubleAvailable = false
                                    gameOverVisible = false
                                    result = null
                                    Feedback.play(Feedback.Sound.REWARD)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (doubleAvailable && res.starsCollected > 0) {
                    OrbitButton(
                        text = Locales.t("double_ad"),
                        background = Color(0xFF2ED3C0),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (activity != null) {
                            AdManager.showRewarded(activity, "double_stars") { earned ->
                                if (earned && engine.doubleStars()) {
                                    doubleAvailable = false
                                    continueAvailable = false
                                    LocalStore.addStars(res.starsCollected)
                                    Feedback.play(Feedback.Sound.REWARD)
                                    Analytics.log("stars_doubled")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OrbitButton(text = Locales.t("retry"), background = Color(0xFF1B2A55), textColor = Color(0xFFF7F3E8), modifier = Modifier.weight(1f)) {
                        Feedback.play(Feedback.Sound.CLICK)
                        finalizeResult()
                        onRestart()
                    }
                    OrbitButton(text = Locales.t("menu"), background = Color(0xFF1B2A55), textColor = Color(0xFFF7F3E8), modifier = Modifier.weight(1f)) {
                        Feedback.play(Feedback.Sound.CLICK)
                        finalizeResult()
                        Feedback.pauseMusic()
                        onExitToMenu()
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                AdManager.BannerAd()
            }
        }
    }
}

/** Voile sombre derrière les panneaux. */
@Composable
private fun Overlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC0B142E))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* absorbe les taps */ }
    )
}
