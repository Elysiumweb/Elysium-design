package com.elysium.orbithop.core.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

/** Événements analytics (section 11 du cahier des charges) et journal Crashlytics. */
object Analytics {

    private var analytics: FirebaseAnalytics? = null

    fun init(context: android.content.Context) {
        analytics = FirebaseAnalytics.getInstance(context)
    }

    fun log(event: String, params: Bundle.() -> Unit = {}) {
        runCatching { analytics?.logEvent(event, Bundle().apply(params)) }
    }

    fun setUserId(uid: String?) {
        runCatching {
            analytics?.setUserId(uid)
            FirebaseCrashlytics.getInstance().setUserId(uid ?: "guest")
        }
    }

    fun note(message: String) {
        runCatching { FirebaseCrashlytics.getInstance().log(message) }
    }
}
