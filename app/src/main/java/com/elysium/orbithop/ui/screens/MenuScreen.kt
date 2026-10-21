package com.elysium.orbithop.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.orbithop.ads.AdManager
import com.elysium.orbithop.core.auth.AuthRepository
import com.elysium.orbithop.core.auth.AuthState
import com.elysium.orbithop.core.i18n.Locales
import com.elysium.orbithop.core.store.LocalStore
import com.elysium.orbithop.core.util.Feedback
import com.elysium.orbithop.ui.components.GhostButton
import com.elysium.orbithop.ui.components.OrbitButton
import com.elysium.orbithop.ui.components.ProgressBar
import com.elysium.orbithop.ui.components.SectionTitle
import com.elysium.orbithop.ui.components.StatBox
import com.elysium.orbithop.ui.theme.PlanetOrange
import com.elysium.orbithop.ui.theme.StarYellow
import com.elysium.orbithop.ui.theme.TextSecondary

@Composable
fun MenuScreen(
    onPlay: () -> Unit,
    onShop: () -> Unit,
    onLeaderboard: () -> Unit,
    onSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    var refreshTick by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101B3C))
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Petit visuel de jeu (planète + orbite + satellite)
        MenuArtwork(modifier = Modifier.fillMaxWidth().height(150.dp))

        Text(
            text = "ORBIT HOP",
            color = Color(0xFFF7F3E8),
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
        Text(
            text = when (val s = AuthRepository.state) {
                is AuthState.SignedIn -> Locales.t("signed_in_as", s.name)
                is AuthState.Guest -> Locales.t("guest_as", LocalStore.guestName)
                else -> ""
            },
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox(label = Locales.t("best"), value = "${LocalStore.bestScore}", modifier = Modifier.weight(1f), accent = PlanetOrange)
            StatBox(label = Locales.t("stars"), value = "${LocalStore.stars}", modifier = Modifier.weight(1f), accent = StarYellow)
        }

        Spacer(modifier = Modifier.height(18.dp))

        OrbitButton(
            text = Locales.t("play"),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 24
        ) {
            Feedback.play(Feedback.Sound.CLICK)
            onPlay()
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GhostButton(text = Locales.t("shop"), modifier = Modifier.weight(1f)) {
                Feedback.play(Feedback.Sound.CLICK); onShop()
            }
            GhostButton(text = Locales.t("leaderboard"), modifier = Modifier.weight(1f)) {
                Feedback.play(Feedback.Sound.CLICK); onLeaderboard()
            }
            GhostButton(text = Locales.t("settings"), modifier = Modifier.weight(1f)) {
                Feedback.play(Feedback.Sound.CLICK); onSettings()
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ---- Récompense de connexion ----
        SectionTitle(text = Locales.t("daily_reward"), modifier = Modifier.fillMaxWidth())
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1B2A55))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = Locales.t("streak", LocalStore.loginStreak),
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (LocalStore.canClaimDailyReward) {
                    val reward = 25 + (minOf(LocalStore.loginStreak, 7)) * 10
                    OrbitButton(
                        text = Locales.t("daily_reward_get", reward),
                        background = StarYellow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val gained = LocalStore.claimDailyReward()
                        if (gained != null) {
                            Feedback.play(Feedback.Sound.REWARD)
                            refreshTick++
                        }
                    }
                } else {
                    Text(
                        text = Locales.t("daily_reward_claimed"),
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ---- Missions du jour ----
        SectionTitle(text = Locales.t("missions"), modifier = Modifier.fillMaxWidth())
        val missions = LocalStore.dailyMissions()
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (mission in missions) {
                key(mission.def.kind to refreshTick) {
                    MissionRow(
                        title = when (mission.def.kind) {
                            com.elysium.orbithop.core.model.MissionKind.STARS -> "★ ${mission.def.target}"
                            com.elysium.orbithop.core.model.MissionKind.GAMES -> "${mission.def.target} ${Locales.t("games")}"
                            com.elysium.orbithop.core.model.MissionKind.SCORE -> "${mission.def.target} ${Locales.t("score")}"
                        },
                        reward = mission.def.reward,
                        progress = mission.progress,
                        target = mission.def.target,
                        claimed = mission.claimed,
                        complete = mission.isComplete,
                        onClaim = {
                            if (LocalStore.claimMission(mission.def) != null) {
                                Feedback.play(Feedback.Sound.REWARD)
                                refreshTick++
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        GhostButton(text = Locales.t("sign_out"), modifier = Modifier.fillMaxWidth()) {
            onSignOut()
        }

        Spacer(modifier = Modifier.height(16.dp))
        AdManager.BannerAd()
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun MissionRow(
    title: String,
    reward: Int,
    progress: Int,
    target: Int,
    claimed: Boolean,
    complete: Boolean,
    onClaim: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1B2A55))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color(0xFFF7F3E8), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            ProgressBar(fraction = progress.toFloat() / target)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$progress / $target  ·  +$reward ★",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        if (claimed) {
            Text(text = "✓", color = StarYellow, fontSize = 18.sp, fontWeight = FontWeight.Black)
        } else {
            GhostButton(text = Locales.t("mission_claim"), enabled = complete, onClick = onClaim)
        }
    }
}

/** Scène décorative du menu (rappel de l'icône). */
@Composable
private fun MenuArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val scale = size.height / 3.4f

        for (i in 1..3) {
            drawCircle(
                color = Color(0xFF2C4192),
                radius = scale * (0.65f + i * 0.42f),
                center = Offset(cx, cy),
                style = Stroke(width = 3f)
            )
        }
        drawCircle(Color(0xFFF4714E), radius = scale * 0.55f, center = Offset(cx, cy))
        // Satellite miniature sur la 3e orbite
        val angle = -0.7f
        val r = scale * 1.49f
        val sx = cx + kotlin.math.cos(angle) * r
        val sy = cy + kotlin.math.sin(angle) * r
        drawCircle(Color(0xFF2ED3C0), radius = scale * 0.16f, center = Offset(sx - scale * 0.18f, sy))
        drawCircle(Color(0xFF2ED3C0), radius = scale * 0.16f, center = Offset(sx + scale * 0.18f, sy))
        drawCircle(Color(0xFFF7F3E8), radius = scale * 0.17f, center = Offset(sx, sy))
        drawCircle(Color(0xFF101B3C), radius = scale * 0.07f, center = Offset(sx, sy))
        // Étoiles
        drawCircle(StarYellow, radius = scale * 0.08f, center = Offset(cx - scale * 1.5f, cy + scale * 0.9f))
        drawCircle(StarYellow, radius = scale * 0.06f, center = Offset(cx - scale * 0.7f, cy + scale * 1.3f))
    }
}
