package com.elysium.orbithop.core.util

import com.elysium.orbithop.ads.AdManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

/**
 * Firebase Remote Config : réglages de fréquence publicitaire pilotables à distance
 * (section 12 du cahier des charges) avec valeurs par défaut embarquées.
 */
object RemoteConfigSync {

    fun fetch() {
        runCatching {
            val rc = FirebaseRemoteConfig.getInstance()
            rc.setConfigSettingsAsync(
                FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600)
                    .build()
            )
            rc.setDefaultsAsync(
                mapOf(
                    "interstitial_game_gap" to 3L,
                    "interstitial_min_seconds" to 60L,
                    "interstitial_session_min_seconds" to 120L
                )
            )
            rc.fetchAndActivate().addOnSuccessListener {
                AdManager.interstitialGameGap =
                    rc.getLong("interstitial_game_gap").toInt().coerceAtLeast(1)
                AdManager.interstitialMinSeconds =
                    rc.getLong("interstitial_min_seconds").toInt().coerceAtLeast(0)
                AdManager.interstitialSessionMinSeconds =
                    rc.getLong("interstitial_session_min_seconds").toInt().coerceAtLeast(0)
                Analytics.note("remote config applied")
            }
        }
    }
}
