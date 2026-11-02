package com.elysium.orbithop.core.model

import androidx.compose.ui.graphics.Color

/** Nature des missions quotidiennes. */
enum class MissionKind { STARS, GAMES, SCORE }

/** Définition fixe d'une mission (cible + récompense en étoiles). */
data class MissionDef(val kind: MissionKind, val target: Int, val reward: Int)

/** État courant d'une mission pour la journée. */
data class Mission(
    val def: MissionDef,
    val progress: Int,
    val claimed: Boolean
) {
    val isComplete: Boolean get() = progress >= def.target
    val fraction: Float get() = (progress.toFloat() / def.target).coerceIn(0f, 1f)
}

/** Ligne du classement Firestore. */
data class ScoreEntry(
    val uid: String = "",
    val name: String = "",
    val score: Int = 0,
    val stars: Int = 0,
    val skin: Int = 0,
    val createdAt: Long = 0
)

/** Résultat d'une partie. */
data class GameResult(
    val score: Int,
    val starsCollected: Int,
    val distanceScore: Int,
    val newBest: Boolean
)

/** Apparence du satellite, déblocable avec des étoiles. */
data class Skin(
    val id: Int,
    val nameFr: String,
    val nameEn: String,
    val price: Int,
    val body: Color,
    val panel: Color
) {
    fun name(langTag: String): String = if (langTag == "fr") nameFr else nameEn
}

object Skins {
    val all: List<Skin> = listOf(
        Skin(0, "Classique", "Classic", 0, Color(0xFFF7F3E8), Color(0xFF2ED3C0)),
        Skin(1, "Solaire", "Solar", 300, Color(0xFFFFD166), Color(0xFFF4714E)),
        Skin(2, "Nébuleuse", "Nebula", 300, Color(0xFFB388FF), Color(0xFF64D8CB)),
        Skin(3, "Mars", "Mars", 600, Color(0xFFFF8A65), Color(0xFF8D6E63)),
        Skin(4, "Glace", "Ice", 600, Color(0xFFE1F5FE), Color(0xFF4FC3F7)),
        Skin(5, "Minuit", "Midnight", 900, Color(0xFF546E7A), Color(0xFF90A4AE)),
        Skin(6, "Émeraude", "Emerald", 900, Color(0xFFA5D6A7), Color(0xFF2E7D32)),
        Skin(7, "Orbit Pro", "Orbit Pro", 1500, Color(0xFFFFE082), Color(0xFFFF7043))
    )

    fun byId(id: Int): Skin = all.firstOrNull { it.id == id } ?: all.first()
}
