// Module application — Orbit Hop
// Build : ouvrir le projet dans Android Studio (Giraffe+ / Meerkat+), puis "Sync Project with Gradle Files".

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")

    // Lit app/google-services.json et injecte la configuration Firebase
    id("com.google.gms.google-services")

    // Rapports de plantage + upload du mapping R8
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.elysium.orbithop"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.elysium.orbithop"
        minSdk = 24
        // Exigence Google Play depuis le 31 août 2026 : targetSdk >= 36 (Android 16)
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        // ==================== CONFIGURATION ADMOB ====================
        // ⚠️ IDs de TEST officiels Google : ils servent des annonces de test.
        // AVANT LA PUBLICATION : remplacez ces 4 valeurs par vos identifiants AdMob
        // (AdMob > Paramètres > Applications, et AdMob > Unités de publicité).
        buildConfigField("String", "ADMOB_BANNER_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
        buildConfigField("String", "ADMOB_REWARDED_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
        manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
        // =============================================================

        // Mettre true pour simuler un utilisateur EEA pendant les tests du consentement UMP
        buildConfigField("boolean", "CONSENT_DEBUG_EEA", "false")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // TODO publication : configurer le keystore de signature (Play Console > App signing)
            // signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    // ---------- Jetpack Compose (BOM) ----------
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")

    // ---------- Firebase (BoM 35.0.2) ----------
    implementation(platform("com.google.firebase:firebase-bom:35.0.2"))
    implementation("com.google.firebase:firebase-analytics")   // Analytics
    implementation("com.google.firebase:firebase-auth")        // Auth e-mail/mdp + Google
    implementation("com.google.firebase:firebase-firestore")   // Données & classement
    implementation("com.google.firebase:firebase-crashlytics") // Plantages
    implementation("com.google.firebase:firebase-config")      // Remote Config (fréquence pubs…)

    // ---------- Publicité ----------
    implementation("com.google.android.gms:play-services-ads:25.5.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0") // Consentement RGPD

    // ---------- Achats intégrés (« Supprimer les pubs ») ----------
    // Play impose la version 8+ depuis le 31 août 2026 pour les nouvelles apps.
    implementation("com.android.billingclient:billing:8.0.0")

    // ---------- Connexion Google (Credential Manager) ----------
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
}
