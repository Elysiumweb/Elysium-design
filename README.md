# Orbit Hop 🛰️

Jeu mobile hyper-casual one-touch : un petit satellite saute d'orbite en orbite pour
ramasser des étoiles sans heurter les débris. Gratuit, financé par la publicité,
publié sur Google Play.

- **Stack** : Kotlin + Jetpack Compose (jeu dessiné en Compose Canvas — aucun code natif C/NDK)
- **Build** : Android Studio (Gradle Kotlin DSL)
- **Backend** : Firebase (Auth, Firestore, Analytics, Crashlytics, Remote Config)
- **Publicité** : AdMob (bannière menus · interstitiel fin de partie · vidéo récompensée)
- **Consentement** : Google UMP (RGPD / TCF)
- **Achat intégrés** : Play Billing 8 — « Supprimer les pubs » + restauration

---

## 1. Ouvrir et builder le projet

1. Ouvrir le dossier du projet dans **Android Studio** (Ladybug ou plus récent).
2. Laisser la synchronisation Gradle télécharger les dépendances
   (Gradle 8.13 via le wrapper fourni — si Android Studio propose « Fix Gradle wrapper », accepter).
3. Choisir un appareil / émulateur (API 24+) puis **Run ▶**.

```bash
./gradlew assembleDebug     # APK de debug
./gradlew assembleRelease   # APK de release (configurer le keystore avant)
./gradlew bundleRelease     # AAB pour la Play Console
```

> Les identifiants **AdMob de test** sont intégrés : des annonces de test s'affichent
> immédiatement, sans configuration. Le jeu est donc testable de bout en bout dès maintenant.

---

## 2. Configuration AdMob (obligatoire avant publication)

Dans `app/build.gradle.kts`, bloc `defaultConfig`, remplacer les **4 valeurs de test**
par vos identifiants AdMob ([AdMob > Paramètres > Applications](https://apps.admob.com)) :

```kotlin
buildConfigField("String", "ADMOB_BANNER_ID", "\"ca-app-pub-XXXX/BBBBBBBBBB\"")
buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-XXXX/IIIIIIIIII\"")
buildConfigField("String", "ADMOB_REWARDED_ID", "\"ca-app-pub-XXXX/RRRRRRRRRR\"")
manifestPlaceholders["admobAppId"] = "ca-app-pub-XXXX~NNNNNNNNNN"
```

Unités de publicité à créer dans AdMob :

| Format | Usage dans le jeu | Règles |
|---|---|---|
| **Bannière** | Menus uniquement (menu, boutique, paramètres, fin de partie) | Zone réservée 50 dp, jamais pendant le jeu |
| **Interstitiel** | Écran de fin de partie | Jamais la 1re session, jamais les 2 premières minutes, min. 60 s entre 2 affichages, 1 tous les 3 game-overs (réglable via Remote Config) |
| **Avec récompense** | « Continuer » après un crash (1×/partie) · « Doubler les étoiles » | Toujours choisie par le joueur, bénéfice annoncé avant |

Si aucune publicité n'est disponible, le jeu continue sans blocage avec un message discret.

---

## 3. Configuration Firebase

`app/google-services.json` est déjà en place (projet `orbit-hop-262a1`, paquet `com.elysium.orbithop`).

### 3.1 Règles Firestore (sécurisées)

Les règles prêtes à déployer sont dans [`firestore.rules`](firestore.rules) :
chaque joueur n'écrit que sur son document `users/{uid}`, les scores sont créés
par leur propriétaire avec validation des bornes, et le classement est en lecture
seule pour les utilisateurs authentifiés. Déploiement :

```bash
firebase deploy --only firestore:rules
```

### 3.2 Authentification

- **E-mail/mot de passe** : actif immédiatement (Firebase Console > Authentication > Sign-in method).
- **Google** : le code est prêt mais nécessite 3 manipulations dans Firebase Console :
  1. Authentication > Sign-in method > **Google > Activer** ;
  2. Ajouter le **SHA-1** du keystore (debug : `./gradlew signingReport`) ;
  3. Récupérer le **Web client ID** (Authentication > Sign-in method > Google) et le coller dans
     `app/src/main/res/values/strings.xml` :

```xml
<string name="google_web_client_id" translatable="false">xxxx.apps.googleusercontent.com</string>
```

Tant que ce champ est vide, le bouton Google affiche un message explicite et
l'e-mail/mot de passe reste fonctionnel.

### 3.3 Remote Config (optionnel)

Valeurs par défaut embarquées ; créer dans Firebase Console pour piloter à distance :

| Clé | Défaut | Rôle |
|---|---|---|
| `interstitial_game_gap` | 3 | Parties entre 2 interstitiels |
| `interstitial_min_seconds` | 60 | Délai minimal entre 2 interstitiels |
| `interstitial_session_min_seconds` | 120 | Délai avant tout interstitiel en session |

### 3.4 ⚠️ Clé de service Firebase Admin

Le fichier `orbit-hop-…-firebase-adminsdk-….json` **ne doit jamais** être versionné ni
embarqué dans l'app Android (il est ignoré par `.gitignore`). Il ne sert qu'à un
éventuel backend (validation des scores) — conservez-le hors dépôt, en lieu sûr.

---

## 4. Achat intégré « Supprimer les pubs »

1. Play Console > **Monétisation > Produits > Produits uniques**.
2. Créer le produit `remove_ads` (identifiant exact), type : produit unique (non consommable).
3. L'achat supprime bannières et interstitiels ; les vidéos récompensées restent
   facultatives (choix du joueur). La restauration sur un nouvel appareil est automatique
   (et disponible via le bouton « Restaurer mes achats »).

---

## 5. Avant la publication — checklist Play Store

- [ ] Remplacer les 4 identifiants AdMob de test par les vrais (§2)
- [ ] Activer Google Sign-In + SHA-1 + web client ID (§3.2) si souhaité
- [ ] Déployer `firestore.rules` (§3.1)
- [ ] Créer le produit `remove_ads` (§4)
- [ ] Remplacer les URLs légales dans `SettingsScreen.kt` (`PRIVACY_URL`, `TERMS_URL`)
   et publier la politique de confidentialité (modèle : `docs/politique-de-confidentialite.md`)
- [ ] Configurer le **keystore de signature** (App signing Play) dans `app/build.gradle.kts`
- [ ] Remplir la fiche **Data safety** : données collectées (e-mail, identifiant publicitaire,
   score), finalité (fonctionnement de l'app, analytique, publicité), chiffrement en transit
- [ ] Questionnaire **IARC** : jeu d'arcade, public visé 16+, aucun contenu violent
- [ ] Écran de consentement RGPD : créer le message dans AdMob > **Confidentialité et messages**
   (formulaire UMP — indispensable pour l'EEE)
- [ ] Captures d'écran (portrait) + icône 512×512 (`docs/`), vidéo optionnelle
- [ ] Test interne puis bêta fermée avant déploiement progressif

---

## 6. Architecture du code

```
app/src/main/java/com/elysium/orbithop/
├── OrbitHopApp.kt          # Application : Firebase, Ads, Billing, sons
├── MainActivity.kt         # Activity unique Compose + consentement UMP
├── ads/                    # AdManager (bannière/interstitiel/récompense), ConsentManager (UMP)
├── billing/                # BillingManager (« Supprimer les pubs » + restauration)
├── core/
│   ├── auth/               # AuthRepository (e-mail/mdp, Google Credential Manager, invité)
│   ├── i18n/               # Locales (textes FR/EN externalisés)
│   ├── model/              # Modèles + skins du satellite
│   ├── store/              # LocalStore (sauvegarde locale F03), FirestoreRepository
│   └── util/               # Sons/vibrations, analytics, Remote Config, await()
├── game/                   # GameEngine (logique pure) + GameCanvas (rendu Compose)
└── ui/                     # AppRoot (navigation), thème, composants, écrans
```

**Boucle de jeu** : 4 orbites, un appui = saut vers l'orbite suivante ; étoiles à
ramasser, débris à éviter ; vitesse croissante, nouveau système solaire (décor +
difficulté) tous les 2 500 points. Score = distance + 15 × étoiles.

---

## 7. Conformité

- Consentement RGPD avant toute publicité (UMP) ; réglage modifiable dans les paramètres.
- Le jeu fonctionne hors ligne (progression locale) ; la connexion est requise uniquement
  pour les publicités, le classement et la synchro du profil.
- Public visé : **16 ans et plus**, aucun mineur ciblé (voir cahier des charges §9).
- Politique de confidentialité et CGU : modèles dans `docs/`, à faire relire par un juriste.
