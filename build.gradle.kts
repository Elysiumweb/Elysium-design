// Fichier Gradle au niveau racine (projet) — Orbit Hop
plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false

    // Plug-in Gradle des services Google (google-services.json -> SDK Firebase)
    id("com.google.gms.google-services") version "4.5.0" apply false

    // Firebase Crashlytics (rapports de plantage + upload du mapping R8)
    id("com.google.firebase.crashlytics") version "3.0.8" apply false
}
