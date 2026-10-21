package com.elysium.orbithop

import android.app.Application
import com.elysium.orbithop.billing.BillingManager
import com.elysium.orbithop.core.auth.AuthRepository
import com.elysium.orbithop.core.store.LocalStore
import com.elysium.orbithop.core.util.Analytics
import com.elysium.orbithop.core.util.Feedback
import com.elysium.orbithop.core.util.RemoteConfigSync
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class OrbitHopApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Firebase (google-services.json)
        FirebaseApp.initializeApp(this)

        // Sauvegarde locale (F03) — doit être prête avant toute lecture UI
        LocalStore.init(this)

        Analytics.init(this)
        Feedback.init(this)
        AuthRepository.init()
        BillingManager.init(this)
        RemoteConfigSync.fetch()

        // Initialisation AdMob en arrière-plan (recommandé par Google)
        applicationScope.launch(Dispatchers.IO) {
            runCatching { MobileAds.initialize(this@OrbitHopApp) }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Feedback.release()
        BillingManager.end()
    }
}
