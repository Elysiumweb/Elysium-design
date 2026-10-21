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

## 2. Configuration AdMob ✅ (intégré)

Compte AdMob : **pub-5316598937383510**. Les identifiants sont câblés dans `app/build.gradle.kts` :

| Format | ID d'unité de publicité | Usage dans le jeu |
|---|---|---|
| **Bannière** | `ca-app-pub-5316598937383510/2509620902` | Menus uniquement (menu, boutique, paramètres, fin de partie) |
| **Interstitiel** | `ca-app-pub-5316598937383510/8487696736` | Écran de fin de partie |
| **Avec récompense** | `ca-app-pub-5316598937383510/3865925440` | « Continuer » après un crash (1×/partie) · « Doubler les étoiles » |

ID d'application : `ca-app-pub-5316598937383510~9596939280`

**Répartition par build** (recommandée par Google) :

- **`debug`** (Run ▶ dans Android Studio) → **IDs de test Google** : annonces de test uniquement,
  aucun risque de clics invalides pendant le développement (un clic sur une vraie pub en test
  peut faire bannir le compte AdMob).
- **`release`** (`assembleRelease` / `bundleRelease`, celui publié sur le Play Store) → **vos IDs de production**.

Règles d'intégration respectées (Règles AdMob) : la bannière n'apparaît jamais pendant le jeu,
l'interstitiel est plafonné et n'apparaît qu'à une pause naturelle, la vidéo récompensée est
toujours facultative avec un bénéfice annoncé. Si aucune publicité n'est disponible, le jeu
continue sans blocage avec un message discret.

### 2.1 Fichier app-ads.txt (obligatoire)

Le fichier [`app-ads.txt`](app-ads.txt) est prêt à la racine du dépôt :

```
google.com, pub-5316598937383510, DIRECT, f08c47fec0942fa0
```

À publier **à la racine du domaine développeur** déclaré dans Google Play (et non dans un
sous-dossier), par exemple `https://elysiumweb.github.io/app-ads.txt` ou
`https://votredomaine.com/app-ads.txt` :

1. Copier `app-ads.txt` à la racine du site (pour GitHub Pages : dans le dépôt qui publie
   la racine du domaine, ex. `elysiumweb.github.io`, ou sur votre domaine personnalisé).
2. Vérifier qu'il est accessible dans un navigateur : `https://<votre-domaine>/app-ads.txt`.
3. Attendre **24 h** (crawl AdMob), puis vérifier l'onglet **app-ads.txt** dans AdMob.

Sans ce fichier, AdMob peut limiter la diffusion des annonces (revenus réduits).

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
- **Google** : ✅ configuré (SHA-1 enregistré, web client ID intégré dans `strings.xml`,
  `google-services.json` à jour). Le bouton « Continuer avec Google » est actif.

> ⚠️ **Avant de publier la version release** : le SHA-1 actuel correspond à ton keystore de
> debug. Ajoute aussi le SHA-1 du **keystore de release** dans Firebase Console
> (Paramètres du projet > Vos applications), puis re-télécharge `google-services.json`
> dans `app/` — sinon la connexion Google échouera sur le build publié.

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

- [x] Identifiants AdMob intégrés (§2) — production dans `release`, test dans `debug`
- [ ] **Publier `app-ads.txt`** à la racine du domaine développeur et vérifier la validation
   dans AdMob (onglet app-ads.txt, ~24 h) — §2.1
- [x] Google Sign-In activé (§3.2) — ajouter le SHA-1 du keystore release + nouveau
   `google-services.json` avant la mise en ligne
- [x] URLs légales en ligne (GitHub Pages) : politique de confidentialité + CGU
   (`https://elysiumweb.github.io/Orbit-Hop/politique-de-confidentialite.html`) — à coller
   dans Play Console > Règles de confidentialité
- [ ] Déployer `firestore.rules` (§3.1)
- [ ] Créer le produit `remove_ads` (§4)
- [x] URLs légales intégrées dans `SettingsScreen.kt` et publiées en ligne (GitHub Pages)
- [x] Suppression de compte : in-app (Paramètres → Supprimer mon compte) + page web
   (`https://elysiumweb.github.io/Orbit-Hop/suppression-de-compte.html`) à coller dans
   Play Console > Sécurité des données > URL de suppression de compte
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
