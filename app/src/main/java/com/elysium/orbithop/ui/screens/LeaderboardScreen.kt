package com.elysium.orbithop.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.orbithop.core.auth.AuthRepository
import com.elysium.orbithop.core.i18n.Locales
import com.elysium.orbithop.core.model.ScoreEntry
import com.elysium.orbithop.core.store.FirestoreRepository
import com.elysium.orbithop.core.store.LocalStore
import com.elysium.orbithop.ui.components.GhostButton
import com.elysium.orbithop.ui.components.SectionTitle
import com.elysium.orbithop.ui.theme.PlanetOrange
import com.elysium.orbithop.ui.theme.StarYellow
import com.elysium.orbithop.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/** Classement mondial des meilleurs scores (Firestore). */
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf(listOf<ScoreEntry>()) }
    var loading by remember { mutableStateOf(true) }

    fun refresh() {
        loading = true
        scope.launch {
            entries = FirestoreRepository.fetchLeaderboard()
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101B3C))
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GhostButton(text = "← ${Locales.t("back")}", onClick = onBack)
            GhostButton(text = Locales.t("refresh"), onClick = { refresh() })
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = Locales.t("leaderboard_title"),
            color = Color(0xFFF7F3E8),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = Locales.t("your_best", LocalStore.bestScore),
            color = TextSecondary,
            fontSize = 13.sp
        )

        if (AuthRepository.currentUid == null) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1B2A55))
                    .padding(12.dp)
            ) {
                Text(
                    text = Locales.t("leaderboard_guest"),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        SectionTitle(text = "TOP 50", modifier = Modifier.fillMaxWidth())

        if (loading) {
            Text(text = Locales.t("loading"), color = TextSecondary, fontSize = 13.sp)
        } else if (entries.isEmpty()) {
            Text(text = Locales.t("leaderboard_empty"), color = TextSecondary, fontSize = 13.sp)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries.withIndex().toList()) { (index, entry) ->
                    val isMe = entry.uid == AuthRepository.currentUid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isMe) Color(0xFF24356E) else Color(0xFF1B2A55))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Locales.t("rank", index + 1),
                            color = if (index < 3) StarYellow else TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.width(46.dp)
                        )
                        Text(
                            text = entry.name,
                            color = if (isMe) PlanetOrange else Color(0xFFF7F3E8),
                            fontSize = 15.sp,
                            fontWeight = if (isMe) FontWeight.Black else FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "★ ${entry.stars}",
                            color = StarYellow,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(
                            text = "${entry.score}",
                            color = Color(0xFFF7F3E8),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
