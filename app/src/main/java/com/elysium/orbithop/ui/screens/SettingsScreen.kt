package com.elysium.orbithop.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.orbithop.ads.ConsentManager
import com.elysium.orbithop.core.i18n.Locales
import com.elysium.orbithop.core.store.LocalStore
import com.elysium.orbithop.core.util.Feedback
import com.elysium.orbithop.ui.components.GhostButton
import com.elysium.orbithop.ui.components.InfoRow
import com.elysium.orbithop.ui.components.SectionTitle
import com.elysium.orbithop.ui.theme.StarYellow

/** URL légales — à remplacer par les vôtres avant la publication (voir README). */
private const val PRIVACY_URL = "https://elysiumweb.github.io/orbit-hop/politique-de-confidentialite.html"
private const val TERMS_URL = "https://elysiumweb.github.io/orbit-hop/conditions-utilisation.html"

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101B3C))
            .verticalScroll(rememberScrollState())
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
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = Locales.t("settings_title"),
            color = Color(0xFFF7F3E8),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ---- Son / Vibrations ----
        SettingToggle(
            label = Locales.t("sound"),
            checked = LocalStore.soundEnabled,
            onToggle = {
                LocalStore.setSound(it)
                Feedback.applySoundSetting()
            }
        )
        SettingToggle(
            label = Locales.t("vibration"),
            checked = LocalStore.vibrationEnabled,
            onToggle = { LocalStore.setVibration(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))
        SectionTitle(text = Locales.t("language"), modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GhostButton(
                text = "Français",
                modifier = Modifier.weight(1f),
                onClick = { LocalStore.setLang("fr") }
            )
            GhostButton(
                text = "English",
                modifier = Modifier.weight(1f),
                onClick = { LocalStore.setLang("en") }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        SectionTitle(text = Locales.t("remove_ads"), modifier = Modifier.fillMaxWidth())
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1B2A55))
                .padding(14.dp)
        ) {
            if (LocalStore.removeAds) {
                Text(
                    text = Locales.t("remove_ads_owned"),
                    color = StarYellow,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            } else {
                Column {
                    GhostButton(text = Locales.t("remove_ads"), modifier = Modifier.fillMaxWidth()) {
                        Toast.makeText(context, Locales.t("shop"), Toast.LENGTH_SHORT).show()
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    GhostButton(text = Locales.t("restore_purchases"), modifier = Modifier.fillMaxWidth()) {
                        com.elysium.orbithop.billing.BillingManager.restorePurchases(context)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        SectionTitle(text = "RGPD", modifier = Modifier.fillMaxWidth())
        GhostButton(text = Locales.t("consent_settings"), modifier = Modifier.fillMaxWidth()) {
            if (activity != null) {
                ConsentManager.showPrivacyOptionsForm(activity)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        SectionTitle(text = Locales.t("about", "1.0.0"), modifier = Modifier.fillMaxWidth())
        GhostButton(text = Locales.t("privacy"), modifier = Modifier.fillMaxWidth()) {
            openUrl(context, PRIVACY_URL)
        }
        Spacer(modifier = Modifier.height(8.dp))
        GhostButton(text = Locales.t("terms"), modifier = Modifier.fillMaxWidth()) {
            openUrl(context, TERMS_URL)
        }
        Spacer(modifier = Modifier.height(8.dp))
        GhostButton(text = Locales.t("replay_tutorial"), modifier = Modifier.fillMaxWidth()) {
            LocalStore.resetTutorial()
            Toast.makeText(context, Locales.t("ok"), Toast.LENGTH_SHORT).show()
        }

        Spacer(modifier = Modifier.height(18.dp))
        InfoRow(left = Locales.t("version", "1.0.0 (1)"), right = "com.elysium.orbithop")
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1B2A55))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFFF7F3E8), fontSize = 16.sp, fontWeight = FontWeight.Medium)
        GhostButton(text = if (checked) "●" else "○", onClick = { onToggle(!checked) })
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Toast.makeText(context, url, Toast.LENGTH_LONG).show()
    }
}
