package com.elysium.orbithop.core.store

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elysium.orbithop.core.model.Mission
import com.elysium.orbithop.core.model.MissionDef
import com.elysium.orbithop.core.model.MissionKind
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Sauvegarde locale automatique (exigence F03) — SharedPreferences.
 * Les valeurs sont exposées via l'état Compose pour une mise à jour réactive de l'UI.
 */
object LocalStore {

    private const val PREFS = "orbit_hop"
    private lateinit var prefs: SharedPreferences

    // ---------------- État observable ----------------
    var stars by mutableStateOf(0)
        private set
    var bestScore by mutableStateOf(0)
        private set
    var gamesPlayed by mutableStateOf(0)
        private set
    var tutorialDone by mutableStateOf(false)
        private set
    var soundEnabled by mutableStateOf(true)
        private set
    var vibrationEnabled by mutableStateOf(true)
        private set
    var langTag by mutableStateOf("fr")
        private set
    var equippedSkin by mutableStateOf(0)
        private set
    var unlockedSkins by mutableStateOf(setOf(0))
        private set
    var removeAds by mutableStateOf(false)
        private set
    var firstLaunchDone by mutableStateOf(false)
        private set
    var loginStreak by mutableStateOf(0)
        private set
    var lastRewardDay by mutableStateOf("")
        private set
    var guestName by mutableStateOf("Pilote")
        private set

    // Compteurs publicitaires (plafonds de fréquence — F07)
    var gamesSinceInterstitial by mutableStateOf(0)
        private set
    var lastInterstitialAt by mutableStateOf(0L)
        private set

    // Missions du jour
    var missionsDate by mutableStateOf("")
        private set
    var missionProgress by mutableStateOf(mapOf<MissionKind, Int>())
        private set
    var missionClaimed by mutableStateOf(setOf<MissionKind>())
        private set

    // ---------------- Session courante (non persisté) ----------------
    var sessionStartMs: Long = System.currentTimeMillis()

    // ---------------- Initialisation ----------------
    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        stars = prefs.getInt("stars", 0)
        bestScore = prefs.getInt("bestScore", 0)
        gamesPlayed = prefs.getInt("gamesPlayed", 0)
        tutorialDone = prefs.getBoolean("tutorialDone", false)
        soundEnabled = prefs.getBoolean("sound", true)
        vibrationEnabled = prefs.getBoolean("vibration", true)
        langTag = prefs.getString("lang", Locale.getDefault().language) ?: "fr"
        if (langTag != "fr" && langTag != "en") langTag = "en"
        equippedSkin = prefs.getInt("equippedSkin", 0)
        unlockedSkins = (prefs.getString("unlockedSkins", "0") ?: "0").split(",").mapNotNull { it.toIntOrNull() }.toSet().ifEmpty { setOf(0) }
        removeAds = prefs.getBoolean("removeAds", false)
        firstLaunchDone = prefs.getBoolean("firstLaunchDone", false)
        loginStreak = prefs.getInt("loginStreak", 0)
        lastRewardDay = prefs.getString("lastRewardDay", "") ?: ""
        guestName = prefs.getString("guestName", "Pilote") ?: "Pilote"
        missionsDate = prefs.getString("missionsDate", "") ?: ""
        missionProgress = MissionKind.entries.associateWith { prefs.getInt("mp_${it.name}", 0) }
        missionClaimed = MissionKind.entries.filter { prefs.getBoolean("mc_${it.name}", false) }.toSet()
        sessionStartMs = System.currentTimeMillis()
        rollDailyIfNeeded()
    }

    private fun edit(block: SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply { block() }.apply()
    }

    // ---------------- Mutations ----------------
    fun addStars(n: Int) {
        stars += n
        edit { putInt("stars", stars) }
    }

    fun spendStars(n: Int): Boolean {
        if (stars < n) return false
        stars -= n
        edit { putInt("stars", stars) }
        return true
    }

    fun recordGameEnd(score: Int, starsEarned: Int) {
        gamesPlayed += 1
        addStars(starsEarned)
        if (score > bestScore) {
            bestScore = score
            edit { putInt("bestScore", bestScore) }
        }
        edit { putInt("gamesPlayed", gamesPlayed) }
        addMissionProgress(MissionKind.GAMES, 1)
        addMissionProgress(MissionKind.STARS, starsEarned)
        addMissionProgress(MissionKind.SCORE, score)
    }

    fun markTutorialDone() {
        if (tutorialDone) return
        tutorialDone = true
        edit { putBoolean("tutorialDone", true) }
    }

    fun setSound(enabled: Boolean) {
        soundEnabled = enabled
        edit { putBoolean("sound", enabled) }
    }

    fun setVibration(enabled: Boolean) {
        vibrationEnabled = enabled
        edit { putBoolean("vibration", enabled) }
    }

    fun setLang(tag: String) {
        langTag = tag
        edit { putString("lang", tag) }
    }

    fun setGuestName(name: String) {
        guestName = name.take(16).ifBlank { "Pilote" }
        edit { putString("guestName", guestName) }
    }

    fun equipSkin(id: Int) {
        if (id in unlockedSkins) {
            equippedSkin = id
            edit { putInt("equippedSkin", id) }
        }
    }

    fun unlockSkin(id: Int): Boolean {
        if (id in unlockedSkins) return true
        val price = com.elysium.orbithop.core.model.Skins.byId(id).price
        if (!spendStars(price)) return false
        unlockedSkins = unlockedSkins + id
        edit { putString("unlockedSkins", unlockedSkins.sorted().joinToString(",")) }
        return true
    }

    fun setRemoveAds(value: Boolean) {
        removeAds = value
        edit { putBoolean("removeAds", value) }
    }

    fun setFirstLaunchDone() {
        firstLaunchDone = true
        edit { putBoolean("firstLaunchDone", true) }
    }

    /** Synchronisation cloud : adopte un record distant plus élevé (restauration multi-appareils). */
    fun forceBestScore(value: Int) {
        if (value > bestScore) {
            bestScore = value
            edit { putInt("bestScore", bestScore) }
        }
    }

    fun noteInterstitialShown() {
        lastInterstitialAt = System.currentTimeMillis()
        gamesSinceInterstitial = 0
        edit {
            putLong("lastInterstitialAt", lastInterstitialAt)
            putInt("gamesSinceInterstitial", 0)
        }
    }

    fun noteGameForInterstitial() {
        gamesSinceInterstitial += 1
        edit { putInt("gamesSinceInterstitial", gamesSinceInterstitial) }
    }

    fun resetTutorial() {
        tutorialDone = false
        edit { putBoolean("tutorialDone", false) }
    }

    // ---------------- Récompense de connexion ----------------
    fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    /** @return la récompense en étoiles, ou null si déjà récupérée aujourd'hui. */
    fun claimDailyReward(): Int? {
        val today = todayKey()
        if (lastRewardDay == today) return null
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            .format(Date(System.currentTimeMillis() - 24L * 3600 * 1000))
        loginStreak = if (lastRewardDay == yesterday) loginStreak + 1 else 1
        lastRewardDay = today
        val reward = 25 + (minOf(loginStreak, 7) - 1) * 10
        addStars(reward)
        edit {
            putInt("loginStreak", loginStreak)
            putString("lastRewardDay", lastRewardDay)
        }
        return reward
    }

    val canClaimDailyReward: Boolean get() = lastRewardDay != todayKey()

    // ---------------- Missions quotidiennes ----------------
    private val missionPool = listOf(
        MissionDef(MissionKind.STARS, 25, 50),
        MissionDef(MissionKind.STARS, 60, 100),
        MissionDef(MissionKind.GAMES, 3, 40),
        MissionDef(MissionKind.GAMES, 6, 80),
        MissionDef(MissionKind.SCORE, 1500, 80),
        MissionDef(MissionKind.SCORE, 3000, 150)
    )

    private fun rollDailyIfNeeded() {
        val today = todayKey()
        if (missionsDate == today) return
        missionsDate = today
        missionProgress = emptyMap()
        missionClaimed = emptySet()
        edit {
            putString("missionsDate", today)
            MissionKind.entries.forEach {
                putInt("mp_${it.name}", 0)
                putBoolean("mc_${it.name}", false)
            }
        }
    }

    /** Trois missions déterministes par jour (mêmes pour un même jour civil). */
    fun dailyMissions(): List<Mission> {
        rollDailyIfNeeded()
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val picks = listOf(
            missionPool[day % missionPool.size],
            missionPool[(day + 2) % missionPool.size],
            missionPool[(day + 4) % missionPool.size]
        )
        return picks.map { def ->
            Mission(
                def = def,
                progress = missionProgress[def.kind] ?: 0,
                claimed = def.kind in missionClaimed
            )
        }
    }

    fun addMissionProgress(kind: MissionKind, amount: Int) {
        rollDailyIfNeeded()
        val newValue = (missionProgress[kind] ?: 0) + amount
        missionProgress = missionProgress + (kind to newValue)
        edit { putInt("mp_${kind.name}", newValue) }
    }

    /** @return la récompense en étoiles, ou null si non réclamable. */
    fun claimMission(def: MissionDef): Int? {
        val progress = missionProgress[def.kind] ?: 0
        if (progress < def.target || def.kind in missionClaimed) return null
        missionClaimed = missionClaimed + def.kind
        edit { putBoolean("mc_${def.kind.name}", true) }
        addStars(def.reward)
        return def.reward
    }

    // ---------------- Utilitaires session ----------------
    fun sessionElapsedMs(): Long = System.currentTimeMillis() - sessionStartMs

    /** Réinitialise l'état invité (changement de compte). */
    fun resetForNewAccount() {
        // On conserve les réglages (son, langue…) mais pas la progression liée au compte.
        stars = 0
        bestScore = 0
        gamesPlayed = 0
        unlockedSkins = setOf(0)
        equippedSkin = 0
        removeAds = false
        edit {
            putInt("stars", 0)
            putInt("bestScore", 0)
            putInt("gamesPlayed", 0)
            putString("unlockedSkins", "0")
            putInt("equippedSkin", 0)
            putBoolean("removeAds", false)
        }
    }
}
