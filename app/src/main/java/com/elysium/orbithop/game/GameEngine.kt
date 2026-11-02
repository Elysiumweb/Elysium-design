package com.elysium.orbithop.game

import com.elysium.orbithop.core.model.GameResult
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Moteur de jeu Orbit Hop — logique pure, sans dépendance Android.
 *
 * Boucle (30 s) : le satellite orbite autour de la planète ; un appui le fait sauter
 * sur l'orbite suivante. Il faut ramasser les étoiles et éviter les débris ;
 * la vitesse augmente avec le temps. La partie s'arrête au contact d'un débris.
 *
 * Repère : unités de jeu, planète au centre ; 4 orbites de rayon [1.05 .. 2.76].
 * L'angle est en radians, sens trigonométrique (écran : y vers le bas, donc horaire visuel).
 */
class GameEngine(private val random: Random = Random.Default) {

    companion object {
        val ORBIT_RADII = floatArrayOf(1.05f, 1.62f, 2.19f, 2.76f)
        const val PLANET_RADIUS = 0.62f
        const val PLAYER_COLLISION = 0.15f
        const val DEBRIS_MIN_SIZE = 0.11f
        const val DEBRIS_MAX_SIZE = 0.18f
        const val STAR_SIZE = 0.13f
        const val HOP_DURATION = 0.26f        // saut d'orbite voisine
        const val WRAP_DURATION = 0.50f       // retour de la 4e orbite à la 1re
        const val INVINCIBLE_AFTER_CONTINUE = 2.5f
        const val SCORE_PER_SECOND = 24f
        const val SCORE_PER_STAR = 15
        const val SYSTEM_SCORE_STEP = 2500f
    }

    // ---------------- État ----------------
    var running = false
        private set
    var paused = false
        private set
    var crashed = false
        private set
    var gameOver = false
        private set
    var time = 0f
        private set

    var playerOrbit = 0
        private set
    var playerAngle = -PI.toFloat() / 2f
        private set
    var hopping = false
        private set
    var hopFrom = 0
        private set
    var hopTo = 0
        private set
    var hopTime = 0f
        private set

    var scoreFloat = 0f
        private set
    var starsCollected = 0
        private set
    var continueUsed = false
        private set
    var doubledStars = false
        private set
    var invincibleTime = 0f
        private set

    val systemIndex: Int get() = (scoreFloat / SYSTEM_SCORE_STEP).toInt()

    // Éléments du décor
    class Debris(
        @JvmField var orbit: Int,
        @JvmField var angle: Float,
        @JvmField var omega: Float,
        @JvmField val size: Float
    )

    class Star(
        @JvmField val orbit: Int,
        @JvmField val angle: Float
    )

    class Particle(
        @JvmField var x: Float,
        @JvmField var y: Float,
        @JvmField var vx: Float,
        @JvmField var vy: Float,
        @JvmField var life: Float,
        @JvmField val maxLife: Float,
        @JvmField val colorIndex: Int
    )

    val debris = mutableListOf<Debris>()
    val stars = mutableListOf<Star>()
    val particles = mutableListOf<Particle>()

    /** Trace du satellite (points [x, y, âge]) pour l'effet de traînée façon icône. */
    val trail = mutableListOf<FloatArray>()

    // ---------------- Cycle de vie ----------------

    fun start() {
        running = true
        paused = false
        crashed = false
        gameOver = false
        time = 0f
        playerOrbit = 0
        playerAngle = -PI.toFloat() / 2f
        hopping = false
        hopTime = 0f
        scoreFloat = 0f
        starsCollected = 0
        continueUsed = false
        doubledStars = false
        invincibleTime = 1.0f // petit délai de grâce au démarrage
        trail.clear()
        particles.clear()
        debris.clear()
        stars.clear()
        repeat(3) { spawnDebris() }
        repeat(3) { spawnStar() }
    }

    fun pause() {
        if (running && !gameOver && !crashed) paused = true
    }

    fun resume() {
        paused = false
    }

    /** Appui du joueur : saut vers l'orbite suivante. */
    fun tap() {
        if (!running || paused || crashed || gameOver || hopping) return
        hopFrom = playerOrbit
        hopTo = (playerOrbit + 1) % ORBIT_RADII.size
        hopping = true
        hopTime = 0f
    }

    /** Poursuite après une vidéo récompensée (1 fois par partie). */
    fun continueAfterReward(): Boolean {
        if (!crashed || continueUsed) return false
        continueUsed = true
        crashed = false
        gameOver = false
        running = true
        hopping = false
        invincibleTime = INVINCIBLE_AFTER_CONTINUE
        // Nettoie les débris dangereux autour du satellite
        val px = playerX()
        val py = playerY()
        debris.removeAll { d ->
            val dx = cos(d.angle) * ORBIT_RADII[d.orbit]
            val dy = sin(d.angle) * ORBIT_RADII[d.orbit]
            hypot(dx - px, dy - py) < 1.1f
        }
        particles.clear()
        while (debris.size < targetDebrisCount()) spawnDebris()
        return true
    }

    /** Doublage des étoiles de la partie (1 fois par partie). */
    fun doubleStars(): Boolean {
        if (gameOver && !doubledStars) {
            doubledStars = true
            return true
        }
        return false
    }

    fun playerX(): Float = cos(playerAngle) * currentRadius()
    fun playerY(): Float = sin(playerAngle) * currentRadius()

    fun currentRadius(): Float {
        if (!hopping) return ORBIT_RADII[playerOrbit]
        val target = ORBIT_RADII[hopTo]
        val from = ORBIT_RADII[hopFrom]
        val t = hopProgress()
        return from + (target - from) * t
    }

    private fun hopProgress(): Float {
        val duration = if (hopTo == 0 && hopFrom == ORBIT_RADII.size - 1) WRAP_DURATION else HOP_DURATION
        return (hopTime / duration).coerceIn(0f, 1f)
    }

    private fun speedOmega(): Float {
        // Vitesse angulaire du satellite : croît avec le temps puis plafonne
        val base = 1.05f
        val growth = 1.35f * min(1f, time / 110f)
        val systemBonus = 0.06f * systemIndex
        return (base + growth + systemBonus).coerceAtMost(2.6f)
    }

    // ---------------- Boucle de mise à jour ----------------

    fun update(dtRaw: Float) {
        if (!running || paused || gameOver) return
        val dt = dtRaw.coerceIn(0f, 0.05f)
        time += dt

        if (invincibleTime > 0f) invincibleTime = (invincibleTime - dt).coerceAtLeast(0f)

        val omega = speedOmega()

        // Avance angulaire
        val angleFactor = if (hopping) 0.88f else 1f
        playerAngle += omega * angleFactor * dt
        if (playerAngle > PI.toFloat() * 2f) playerAngle -= PI.toFloat() * 2f

        // Saut
        if (hopping) {
            hopTime += dt
            if (hopProgress() >= 1f) {
                hopping = false
                playerOrbit = hopTo
                hopTime = 0f
            }
        }

        // Score de distance
        if (!crashed) {
            scoreFloat += SCORE_PER_SECOND * (omega / 1.05f) * dt
        }

        // Débris
        for (d in debris) {
            d.angle += d.omega * dt
            if (d.angle > PI.toFloat() * 2f) d.angle -= PI.toFloat() * 2f
            if (d.angle < 0f) d.angle += PI.toFloat() * 2f
        }

        // Traînée
        if (!crashed) {
            trail.add(floatArrayOf(playerX(), playerY(), 0f))
            if (trail.size > 14) trail.removeAt(0)
        }
        for (t in trail) t[2] += dt

        // Particules
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.life -= dt
            if (p.life <= 0f) iterator.remove() else {
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.vx *= 0.96f
                p.vy *= 0.96f
            }
        }

        if (crashed) return

        // Collisions / collectes (distance cartésienne en unités de jeu)
        val px = playerX()
        val py = playerY()

        for (d in debris) {
            val dx = cos(d.angle) * ORBIT_RADII[d.orbit]
            val dy = sin(d.angle) * ORBIT_RADII[d.orbit]
            if (hypot(dx - px, dy - py) < d.size + PLAYER_COLLISION) {
                if (invincibleTime <= 0f) {
                    onCrash()
                    return
                }
            }
        }

        val starIterator = stars.iterator()
        while (starIterator.hasNext()) {
            val s = starIterator.next()
            val sx = cos(s.angle) * ORBIT_RADII[s.orbit]
            val sy = sin(s.angle) * ORBIT_RADII[s.orbit]
            if (hypot(sx - px, sy - py) < STAR_SIZE + PLAYER_COLLISION) {
                starIterator.remove()
                starsCollected += 1
                spawnStar()
            }
        }
    }

    private fun onCrash() {
        crashed = true
        val px = playerX()
        val py = playerY()
        repeat(18) {
            val angle = random.nextFloat() * PI.toFloat() * 2f
            val speed = 0.8f + random.nextFloat() * 2.2f
            particles.add(
                Particle(
                    x = px, y = py,
                    vx = cos(angle) * speed, vy = sin(angle) * speed,
                    life = 0.7f + random.nextFloat() * 0.5f,
                    maxLife = 1.2f,
                    colorIndex = random.nextInt(3)
                )
            )
        }
    }

    /** Passe l'écran de fin de partie en « terminé » (après affichage du résultat). */
    fun finishGameOver() {
        gameOver = true
        running = false
    }

    fun result(previousBest: Int): GameResult {
        val starsScore = starsCollected * SCORE_PER_STAR
        val total = scoreFloat.toInt() + starsScore
        return GameResult(
            score = total,
            starsCollected = starsCollected,
            distanceScore = scoreFloat.toInt(),
            newBest = total > previousBest
        )
    }

    // ---------------- Génération ----------------

    private fun targetDebrisCount(): Int = min(11, 3 + (time / 14f).toInt() + systemIndex)

    private fun spawnDebris() {
        val orbit = random.nextInt(ORBIT_RADII.size)
        var angle = random.nextFloat() * PI.toFloat() * 2f
        // Évite le spawn à côté du satellite
        if (orbit == playerOrbit && kotlin.math.abs(angleDiff(angle, playerAngle)) < 0.9f) {
            angle += 1.4f
        }
        val omega = (random.nextFloat() - 0.5f) * 0.7f
        val size = DEBRIS_MIN_SIZE + random.nextFloat() * (DEBRIS_MAX_SIZE - DEBRIS_MIN_SIZE)
        debris.add(Debris(orbit, angle, omega, size))
    }

    private fun spawnStar() {
        val orbit = random.nextInt(ORBIT_RADII.size)
        val angle = random.nextFloat() * PI.toFloat() * 2f
        stars.add(Star(orbit, angle))
    }

    private fun angleDiff(a: Float, b: Float): Float {
        var d = a - b
        while (d > PI.toFloat()) d -= 2f * PI.toFloat()
        while (d < -PI.toFloat()) d += 2f * PI.toFloat()
        return d
    }
}

/** Palette de couleurs par système solaire (nouveaux décors à chaque palier). */
data class Palette(
    val bg: Long,
    val ring: Long,
    val planet: Long,
    val planetDark: Long,
    val debris: Long,
    val debrisDark: Long
)

object Palettes {
    val all = listOf(
        Palette(0xFF101B3C, 0xFF2C4192, 0xFFF4714E, 0xFFE05B3E, 0xFF7E8AA2, 0xFF5C6880),
        Palette(0xFF0D2137, 0xFF20558A, 0xFF2ED3C0, 0xFF1FA898, 0xFF8FA3BF, 0xFF677C99),
        Palette(0xFF1A1030, 0xFF4A2E8C, 0xFFB388FF, 0xFF8B63D9, 0xFF9B8AB8, 0xFF725F92),
        Palette(0xFF241208, 0xFF8C4A2E, 0xFFFFB347, 0xFFE08A2E, 0xFFAD8A6A, 0xFF82664A)
    )

    fun forSystem(index: Int): Palette = all[index % all.size]
}
