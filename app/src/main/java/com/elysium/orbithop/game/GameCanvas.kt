package com.elysium.orbithop.game

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.elysium.orbithop.core.model.Skins
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Rendu du jeu en Jetpack Compose (Canvas) — direction artistique plate façon icône :
 * fond bleu nuit, orbites bleu roi, planète orange à croissant, satellite crème + panneaux turquoise.
 */
@Composable
fun GameCanvas(
    engine: GameEngine,
    frameTick: Float,
    skinId: Int,
    modifier: Modifier = Modifier
) {
    // frameTick est capturé pour invalider le Canvas à chaque image
    Canvas(modifier = modifier) {
        @Suppress("UNUSED_EXPRESSION")
        frameTick

        val palette = Palettes.forSystem(engine.systemIndex)
        val cx = size.width / 2f
        val cy = size.height * 0.46f
        val scale = min(size.width / 6.1f, size.height / 7.0f)

        // Fond
        drawRect(color = Color(palette.bg))

        // Poussières d'étoiles de fond
        drawBackgroundStars(cx, cy, scale, engine.time)

        // Orbites
        for (r in GameEngine.ORBIT_RADII) {
            drawCircle(
                color = Color(palette.ring),
                radius = r * scale,
                center = Offset(cx, cy),
                style = Stroke(width = min(6f, scale * 0.035f), cap = StrokeCap.Round)
            )
        }

        // Planète + croissant d'ombre
        drawCircle(Color(palette.planet), radius = GameEngine.PLANET_RADIUS * scale, center = Offset(cx, cy))
        withTransform({
            clipPath(Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        cx - GameEngine.PLANET_RADIUS * scale,
                        cy - GameEngine.PLANET_RADIUS * scale,
                        cx + GameEngine.PLANET_RADIUS * scale,
                        cy + GameEngine.PLANET_RADIUS * scale
                    )
                )
            })
        }) {
            drawCircle(
                Color(palette.planetDark),
                radius = GameEngine.PLANET_RADIUS * scale,
                center = Offset(cx + GameEngine.PLANET_RADIUS * 0.32f * scale, cy + GameEngine.PLANET_RADIUS * 0.32f * scale)
            )
        }

        // Étoiles à ramasser (étincelles 4 branches jaunes)
        for (star in engine.stars) {
            val x = cx + cos(star.angle) * GameEngine.ORBIT_RADII[star.orbit] * scale
            val y = cy + sin(star.angle) * GameEngine.ORBIT_RADII[star.orbit] * scale
            drawSparkle(Offset(x, y), GameEngine.STAR_SIZE * scale * 1.25f, Color(0xFFFFD166))
        }

        // Débris
        for (d in engine.debris) {
            val x = cx + cos(d.angle) * GameEngine.ORBIT_RADII[d.orbit] * scale
            val y = cy + sin(d.angle) * GameEngine.ORBIT_RADII[d.orbit] * scale
            val radius = d.size * scale
            drawCircle(Color(palette.debris), radius = radius, center = Offset(x, y))
            drawCircle(
                Color(palette.debrisDark),
                radius = radius * 0.32f,
                center = Offset(x - radius * 0.3f, y - radius * 0.25f)
            )
            drawCircle(
                Color(palette.debrisDark),
                radius = radius * 0.22f,
                center = Offset(x + radius * 0.35f, y + radius * 0.3f)
            )
        }

        // Traînée du satellite (points turquoise décroissants)
        val skin = Skins.byId(skinId)
        for ((index, point) in engine.trail.withIndex()) {
            val age = point[2].coerceIn(0f, 1f)
            val alpha = (1f - age) * (index.toFloat() / engine.trail.size.coerceAtLeast(1))
            if (alpha > 0.02f) {
                drawCircle(
                    color = skin.panel.copy(alpha = alpha * 0.8f),
                    radius = scale * 0.045f * (0.4f + alpha),
                    center = Offset(cx + point[0] * scale, cy + point[1] * scale)
                )
            }
        }

        // Satellite
        if (!engine.crashed) {
            val px = cx + engine.playerX() * scale
            val py = cy + engine.playerY() * scale
            val blink = engine.invincibleTime > 0f && (engine.time * 12).toInt() % 2 == 0
            if (!blink) {
                drawSatellite(
                    center = Offset(px, py),
                    rotation = engine.playerAngle + Math.PI.toFloat() / 2f,
                    size = scale * 0.42f,
                    body = skin.body,
                    panel = skin.panel
                )
            }
        }

        // Particules d'explosion
        for (p in engine.particles) {
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            val color = when (p.colorIndex) {
                0 -> Color(0xFFF7F3E8)
                1 -> Color(0xFFF4714E)
                else -> Color(0xFF2ED3C0)
            }
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = scale * 0.05f * alpha,
                center = Offset(cx + p.x * scale, cy + p.y * scale)
            )
        }
    }
}

private fun DrawScope.drawBackgroundStars(cx: Float, cy: Float, scale: Float, time: Float) {
    // Grille déterministe de petites étoiles blanches
    val cols = 9
    val rows = 14
    for (i in 0 until cols) {
        for (j in 0 until rows) {
            val seed = (i * 31 + j * 17) % 97
            val x = cx - scale * 3.2f + i * scale * 0.78f + (seed % 7) * scale * 0.05f
            val y = cy - scale * 3.6f + j * scale * 0.52f + (seed % 5) * scale * 0.06f
            val twinkle = 0.35f + 0.35f * sin(time * 1.3f + seed)
            drawCircle(
                color = Color(0xFFF7F3E8).copy(alpha = twinkle * 0.55f),
                radius = scale * 0.016f * (1 + (seed % 3) * 0.4f),
                center = Offset(x, y)
            )
        }
    }
}

/** Étincelle 4 branches (comme les étoiles de l'icône). */
private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color) {
    val inner = radius * 0.2f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + inner, center.y - inner)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + inner, center.y + inner)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - inner, center.y + inner)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - inner, center.y - inner)
        close()
    }
    drawPath(path, color = color)
}

/** Satellite façon icône : corps crème avec hublot circulaire + 2 panneaux turquoise quadrillés. */
private fun DrawScope.drawSatellite(
    center: Offset,
    rotation: Float,
    size: Float,
    body: Color,
    panel: Color
) {
    withTransform({ rotate(degrees = rotation * 57.29578f, pivot = center) }) {
        val panelW = size * 0.52f
        val panelH = size * 0.78f
        val gap = size * 0.62f
        val bodyW = size * 0.62f
        val bodyH = size * 0.52f

        // Panneaux solaires (grille 2x2)
        for (side in listOf(-1, 1)) {
            val left = center.x + side * gap - panelW / 2f
            val top = center.y - panelH / 2f
            drawRect(
                color = panel,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(panelW, panelH)
            )
            val dark = panel.copy(alpha = 0.45f)
            drawLine(dark, Offset(left, center.y), Offset(left + panelW, center.y), strokeWidth = size * 0.045f)
            drawLine(dark, Offset(left + panelW / 2f, top), Offset(left + panelW / 2f, top + panelH), strokeWidth = size * 0.045f)
            // Lien avec le corps
            drawLine(
                body,
                Offset(center.x + side * bodyW * 0.4f, center.y),
                Offset(left + (if (side > 0) 0f else panelW), center.y),
                strokeWidth = size * 0.1f
            )
        }

        // Corps (rectangle arrondi simulé par deux rectangles + disque)
        val bodyColor = body
        drawRect(
            color = bodyColor,
            topLeft = Offset(center.x - bodyW / 2f, center.y - bodyH / 2f),
            size = androidx.compose.ui.geometry.Size(bodyW, bodyH)
        )
        drawCircle(bodyColor, radius = bodyH / 2f, center = Offset(center.x - bodyW / 2f, center.y))
        drawCircle(bodyColor, radius = bodyH / 2f, center = Offset(center.x + bodyW / 2f, center.y))

        // Hublot circulaire
        drawCircle(Color(0xFF101B3C), radius = bodyH * 0.34f, center = center)
        drawCircle(bodyColor.copy(alpha = 0.25f), radius = bodyH * 0.42f, center = center, style = Stroke(width = size * 0.03f))
    }
}
