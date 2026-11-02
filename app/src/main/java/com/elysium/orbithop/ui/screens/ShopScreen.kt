package com.elysium.orbithop.ui.screens

import android.app.Activity
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.orbithop.billing.BillingManager
import com.elysium.orbithop.core.i18n.Locales
import com.elysium.orbithop.core.model.Skins
import com.elysium.orbithop.core.store.LocalStore
import com.elysium.orbithop.core.util.Feedback
import com.elysium.orbithop.ui.components.GhostButton
import com.elysium.orbithop.ui.components.OrbitButton
import com.elysium.orbithop.ui.components.SectionTitle
import com.elysium.orbithop.ui.components.SkinPreview
import com.elysium.orbithop.ui.theme.PlanetOrange
import com.elysium.orbithop.ui.theme.StarYellow
import com.elysium.orbithop.ui.theme.TextSecondary

/** Boutique : apparences du satellite (déblocables avec des étoiles) + suppression des pubs. */
@Composable
fun ShopScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
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
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GhostButton(text = "← ${Locales.t("back")}", onClick = onBack)
            Text(
                text = "${Locales.t("stars")} : ${LocalStore.stars} ★",
                color = StarYellow,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = Locales.t("shop_title"),
            color = Color(0xFFF7F3E8),
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Grille 2 colonnes de skins
        val rows = Skins.all.chunked(2)
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (skin in row) {
                    key(skin.id to refreshTick) {
                        SkinCard(
                            modifier = Modifier.weight(1f),
                            name = skin.name(LocalStore.langTag),
                            price = skin.price,
                            unlocked = skin.id in LocalStore.unlockedSkins,
                            equipped = LocalStore.equippedSkin == skin.id,
                            skin = skin,
                            onAction = {
                                if (skin.id in LocalStore.unlockedSkins) {
                                    LocalStore.equipSkin(skin.id)
                                    Feedback.play(Feedback.Sound.CLICK)
                                } else {
                                    if (LocalStore.unlockSkin(skin.id)) {
                                        LocalStore.equipSkin(skin.id)
                                        Feedback.play(Feedback.Sound.REWARD)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            Locales.t("not_enough_stars"),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                refreshTick++
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))
        SectionTitle(text = Locales.t("remove_ads"), modifier = Modifier.fillMaxWidth())
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1B2A55))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = Locales.t("remove_ads_desc"),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (LocalStore.removeAds) {
                    Text(
                        text = Locales.t("remove_ads_owned"),
                        color = StarYellow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    OrbitButton(
                        text = Locales.t("remove_ads"),
                        background = PlanetOrange,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (activity != null) {
                            BillingManager.purchase(activity) { code ->
                                if (code == "PRODUCT_UNAVAILABLE") {
                                    Toast.makeText(
                                        context,
                                        Locales.t("purchase_error", "produit indisponible"),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    GhostButton(text = Locales.t("restore_purchases"), modifier = Modifier.fillMaxWidth()) {
                        BillingManager.restorePurchases(context)
                        Toast.makeText(context, Locales.t("loading"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SkinCard(
    modifier: Modifier = Modifier,
    name: String,
    price: Int,
    unlocked: Boolean,
    equipped: Boolean,
    skin: com.elysium.orbithop.core.model.Skin,
    onAction: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1B2A55))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkinPreview(skin = skin)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = Color(0xFFF7F3E8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        when {
            equipped -> Text(
                text = Locales.t("equipped"),
                color = StarYellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            unlocked -> GhostButton(text = Locales.t("equip"), onClick = onAction)
            else -> GhostButton(text = Locales.t("unlock", price), onClick = onAction)
        }
    }
}
