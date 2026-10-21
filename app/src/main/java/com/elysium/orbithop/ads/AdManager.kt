package com.elysium.orbithop.ads

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.elysium.orbithop.BuildConfig
import com.elysium.orbithop.core.i18n.Locales
import com.elysium.orbithop.core.store.LocalStore
import com.elysium.orbithop.core.util.Analytics
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Gestionnaire AdMob (bannière / interstitiel / récompense).
 *
 * Règles d'expérience joueur (câhier des charges §7.3) :
 *  - interstitiel : jamais la première session, jamais pendant les 2 premières minutes,
 *    au minimum 60 s entre deux affichages, au plus 1 tous les 3 game-overs ;
 *  - bannière : menus uniquement (zone réservée) ;
 *  - récompense : toujours choisie par le joueur.
 */
object AdManager {

    // Plafonds pilotables à distance (Firebase Remote Config), valeurs par défaut ici.
    private const val DEFAULT_INTERSTITIAL_GAME_GAP = 3
    private const val DEFAULT_INTERSTITIAL_MIN_SECONDS = 60
    private const val DEFAULT_INTERSTITIAL_SESSION_MIN_SECONDS = 120

    var interstitialGameGap = DEFAULT_INTERSTITIAL_GAME_GAP
    var interstitialMinSeconds = DEFAULT_INTERSTITIAL_MIN_SECONDS
    var interstitialSessionMinSeconds = DEFAULT_INTERSTITIAL_SESSION_MIN_SECONDS

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    /** Appelée après le consentement : précharge les publicités. */
    fun preload(context: Context) {
        if (!ConsentManager.canRequestAds) return
        loadInterstitial(context)
        loadRewarded(context)
    }

    fun loadInterstitial(context: Context) {
        if (!ConsentManager.canRequestAds) return
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Analytics.note("interstitial loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Analytics.note("interstitial failed: ${error.message}")
                }
            }
        )
    }

    fun loadRewarded(context: Context) {
        if (!ConsentManager.canRequestAds) return
        RewardedAd.load(
            context,
            BuildConfig.ADMOB_REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Analytics.note("rewarded loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Analytics.note("rewarded failed: ${error.message}")
                }
            }
        )
    }

    /** Décide si un interstitiel peut être affiché (plafonds de fréquence). */
    fun shouldShowInterstitial(): Boolean {
        if (!ConsentManager.canRequestAds) return false
        if (LocalStore.removeAds) return false
        if (!LocalStore.firstLaunchDone) return false                       // jamais la 1re session
        if (LocalStore.sessionElapsedMs() < interstitialSessionMinSeconds * 1000L) return false
        if (LocalStore.gamesSinceInterstitial < interstitialGameGap) return false
        if (System.currentTimeMillis() - LocalStore.lastInterstitialAt < interstitialMinSeconds * 1000L) return false
        return true
    }

    /**
     * Affiche l'interstitiel si les conditions sont réunies.
     * @return true si une publicité a été lancée.
     */
    fun showInterstitialIfAllowed(activity: Activity, onDismissed: () -> Unit = {}): Boolean {
        if (!shouldShowInterstitial()) return false
        val ad = interstitialAd ?: return false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                LocalStore.noteInterstitialShown()
                Analytics.log("ad_interstitial_shown")
                loadInterstitial(activity)
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                interstitialAd = null
                loadInterstitial(activity)
                onDismissed()
            }
        }
        ad.show(activity)
        return true
    }

    /**
     * Affiche une vidéo récompensée.
     * @param onResult(true) si la récompense est gagnée ; (false) sinon (fermeture/échec).
     */
    fun showRewarded(activity: Activity, placement: String, onResult: (Boolean) -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            loadRewarded(activity)
            Toast.makeText(activity, Locales.t("ad_unavailable"), Toast.LENGTH_SHORT).show()
            onResult(false)
            return
        }
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded(activity)
                onResult(earned)
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                rewardedAd = null
                loadRewarded(activity)
                onResult(false)
            }
        }
        ad.show(activity) { reward ->
            earned = reward.amount > 0
            Analytics.log("ad_reward_earned") { putString("placement", placement) }
        }
        Analytics.log("ad_reward_shown") { putString("placement", placement) }
    }

    /** Bannière des menus : zone réservée de 50 dp, masquée si « Supprimer les pubs ». */
    @Composable
    fun BannerAd(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (ConsentManager.canRequestAds && !LocalStore.removeAds) {
                val context = LocalContext.current
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = BuildConfig.ADMOB_BANNER_ID
                            adListener = object : AdListener() {
                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    Analytics.note("banner failed: ${error.message}")
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
        }
    }
}
