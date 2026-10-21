package com.elysium.orbithop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elysium.orbithop.core.model.Skin
import com.elysium.orbithop.ui.theme.CosmicNavyDeep
import com.elysium.orbithop.ui.theme.PlanetOrange
import com.elysium.orbithop.ui.theme.StarYellow
import com.elysium.orbithop.ui.theme.TextSecondary

/** Bouton principal du jeu (gros, arrondi, lisible au pouce). */
@Composable
fun OrbitButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    background: Color = PlanetOrange,
    textColor: Color = CosmicNavyDeep,
    fontSize: Int = 18,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (enabled) background else background.copy(alpha = 0.35f))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

/** Bouton secondaire (contour discret). */
@Composable
fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1B2A55).copy(alpha = if (enabled) 1f else 0.4f))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) TextSecondary else TextSecondary.copy(alpha = 0.5f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/** Carte de statistique (record, étoiles…). */
@Composable
fun StatBox(label: String, value: String, modifier: Modifier = Modifier, accent: Color = StarYellow) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1B2A55))
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, color = accent, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(text = label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

/** Titre de section des menus. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

/** Barre de progression simple (missions). */
@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, color: Color = StarYellow) {
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1B2A55))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}

/** Aperçu du satellite (boutique, cartes). */
@Composable
fun SkinPreview(skin: Skin, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(64.dp)) {
        val s = size.minDimension * 0.16f
        val c = Offset(size.width / 2f, size.height / 2f)
        // Panneaux
        for (side in listOf(-1, 1)) {
            drawRect(
                color = skin.panel,
                topLeft = Offset(c.x + side * s * 2.6f - s * 1.1f, c.y - s * 1.7f),
                size = androidx.compose.ui.geometry.Size(s * 2.2f, s * 3.4f)
            )
        }
        // Corps
        drawCircle(skin.body, radius = s * 1.6f, center = c)
        drawCircle(CosmicNavyDeep, radius = s * 0.62f, center = c)
        drawCircle(skin.body.copy(alpha = 0.3f), radius = s * 0.9f, center = c, style = Stroke(width = s * 0.18f))
    }
}

/** Ligne clé/valeur. */
@Composable
fun InfoRow(left: String, right: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = left, color = TextSecondary, fontSize = 14.sp)
        Text(text = right, color = Color(0xFFF7F3E8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

/** Style de titre d'écran. */
val ScreenTitleStyle: TextStyle
    get() = TextStyle(
        color = Color(0xFFF7F3E8),
        fontSize = 26.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp
    )
