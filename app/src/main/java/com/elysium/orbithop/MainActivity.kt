package com.elysium.orbithop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.elysium.orbithop.ads.AdManager
import com.elysium.orbithop.ads.ConsentManager
import com.elysium.orbithop.core.util.Analytics
import com.elysium.orbithop.ui.AppRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1) Consentement RGPD (UMP) AVANT toute publicité, puis 2) préchargement des ads.
        ConsentManager.request(this) {
            AdManager.preload(this)
        }

        setContent {
            AppRoot()
        }
    }

    override fun onResume() {
        super.onResume()
        Analytics.note("main resumed")
    }
}
