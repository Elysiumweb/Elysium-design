package com.elysium.orbithop.ads

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elysium.orbithop.BuildConfig
import com.elysium.orbithop.core.util.Analytics
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * Consentement RGPD / ePrivacy via la CMP officielle Google (UMP, compatible TCF).
 * Les publicités ne sont demandées que si [canRequestAds] est vrai.
 */
object ConsentManager {

    var canRequestAds by mutableStateOf(false)
        private set

    private var consentInformation: ConsentInformation? = null

    /** À appeler au démarrage, AVANT toute initialisation publicitaire. */
    fun request(activity: Activity, onDone: () -> Unit = {}) {
        val debugSettings = ConsentDebugSettings.Builder(activity).apply {
            if (BuildConfig.CONSENT_DEBUG_EEA) {
                setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            }
        }.build()

        val params = ConsentRequestParameters.Builder()
            .setTagForChildDirectedTreatment(false)
            .setTagForUnderAgeOfConsent(false)
            .setConsentDebugSettings(debugSettings)
            .build()

        val info = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation = info

        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Infos mises à jour : afficher le formulaire si requis
                if (info.isConsentFormAvailable &&
                    info.consentStatus == ConsentInformation.ConsentStatus.REQUIRED
                ) {
                    UserMessagingPlatform.loadConsentForm(
                        activity,
                        { form ->
                            form.show(activity) { formError ->
                                if (formError != null) {
                                    Analytics.note("UMP form error: ${formError.message}")
                                }
                                updateCanRequestAds()
                                onDone()
                            }
                        },
                        { loadError ->
                            Analytics.note("UMP load error: ${loadError.message}")
                            updateCanRequestAds()
                            onDone()
                        }
                    )
                } else {
                    updateCanRequestAds()
                    onDone()
                }
            },
            { error ->
                Analytics.note("UMP update error: ${error.message}")
                updateCanRequestAds()
                onDone()
            }
        )
    }

    private fun updateCanRequestAds() {
        val status = consentInformation?.consentStatus
        canRequestAds = status == ConsentInformation.ConsentStatus.OBTAINED ||
            status == ConsentInformation.ConsentStatus.NOT_REQUIRED
    }

    /** Réaffiche le formulaire de consentement (depuis les paramètres). */
    fun showPrivacyOptionsForm(activity: Activity, onDone: () -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) Analytics.note("UMP privacy options: ${error.message}")
            updateCanRequestAds()
            onDone()
        }
    }
}
