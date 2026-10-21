package com.elysium.orbithop.core.store

import android.util.Log
import com.elysium.orbithop.core.model.ScoreEntry
import com.elysium.orbithop.core.util.await
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Accès Cloud Firestore — profil joueur, scores, classement.
 * Toutes les opérations sont tolérantes aux pannes : en cas d'absence réseau,
 * le jeu reste jouable localement (exigence « hors ligne »).
 *
 * Collections :
 *  - users/{uid}   : profil, étoiles, skins, réglages (écriture = propriétaire uniquement)
 *  - scores/{doc}  : scores soumis au classement (création = propriétaire uniquement)
 */
object FirestoreRepository {

    private const val TAG = "Firestore"
    private val db get() = FirebaseFirestore.getInstance()

    /** À appeler à la connexion : crée/met à jour le doc utilisateur et fusionne la progression. */
    suspend fun onSignedIn(uid: String, name: String, email: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val ref = db.collection("users").document(uid)
                val snapshot = ref.get().await()
                if (!snapshot.exists()) {
                    val create = hashMapOf<String, Any>(
                        "name" to name,
                        "email" to email,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    ref.set(create, SetOptions.merge()).await()
                } else {
                    // Fusionne le meilleur des deux côtés (restauration F15 / multi-appareils)
                    val remoteStars = (snapshot.getLong("stars") ?: 0L).toInt()
                    val remoteBest = (snapshot.getLong("bestScore") ?: 0L).toInt()
                    if (remoteStars > LocalStore.stars) LocalStore.addStars(remoteStars - LocalStore.stars)
                    if (remoteBest > LocalStore.bestScore) {
                        // Le score local est promu via recordGameEnd uniquement ; ici on force la synchro
                        LocalStore.forceBestScore(remoteBest)
                    }
                    val remoteRemoveAds = snapshot.getBoolean("removeAds") ?: false
                    if (remoteRemoveAds) LocalStore.setRemoveAds(true)
                    pushProfile(uid)
                }
            }.onFailure { Log.w(TAG, "onSignedIn: ${it.message}") }
        }
    }

    /** Pousse l'état local vers Firestore. */
    suspend fun pushProfile(uid: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val data = hashMapOf<String, Any>(
                    "name" to LocalStore.guestName,
                    "stars" to LocalStore.stars,
                    "bestScore" to LocalStore.bestScore,
                    "gamesPlayed" to LocalStore.gamesPlayed,
                    "equippedSkin" to LocalStore.equippedSkin,
                    "unlockedSkins" to LocalStore.unlockedSkins.sorted(),
                    "removeAds" to LocalStore.removeAds,
                    "sound" to LocalStore.soundEnabled,
                    "vibration" to LocalStore.vibrationEnabled,
                    "lang" to LocalStore.langTag,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).set(data, SetOptions.merge()).await()
            }.onFailure { Log.w(TAG, "pushProfile: ${it.message}") }
        }
    }

    /** Soumet un score au classement. */
    suspend fun submitScore(uid: String, entry: ScoreEntry) {
        withContext(Dispatchers.IO) {
            runCatching {
                val data = hashMapOf<String, Any>(
                    "uid" to uid,
                    "name" to entry.name,
                    "score" to entry.score,
                    "stars" to entry.stars,
                    "skin" to entry.skin,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("scores").add(data).await()
            }.onFailure { Log.w(TAG, "submitScore: ${it.message}") }
        }
    }

    /** Récupère le top des scores (index mono-champ automatique sur score DESC). */
    suspend fun fetchLeaderboard(limit: Long = 50): List<ScoreEntry> =
        withContext(Dispatchers.IO) {
            runCatching {
                val snapshot = db.collection("scores")
                    .orderBy("score", Query.Direction.DESCENDING)
                    .limit(limit)
                    .get()
                    .await()
                snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString("uid") ?: return@mapNotNull null
                    ScoreEntry(
                        uid = uid,
                        name = doc.getString("name") ?: "Pilote",
                        score = (doc.getLong("score") ?: 0L).toInt(),
                        stars = (doc.getLong("stars") ?: 0L).toInt(),
                        skin = (doc.getLong("skin") ?: 0L).toInt(),
                        createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                    )
                }
            }.getOrElse {
                Log.w(TAG, "fetchLeaderboard: ${it.message}")
                emptyList()
            }
        }

    /**
     * Supprime toutes les données Firestore d'un compte (suppression de compte) :
     * entrées du classement puis document profil.
     */
    suspend fun deleteAccountData(uid: String) {
        withContext(Dispatchers.IO) {
            val scores = db.collection("scores")
                .whereEqualTo("uid", uid)
                .get()
                .await()
            for (doc in scores.documents) {
                doc.reference.delete().await()
            }
            db.collection("users").document(uid).delete().await()
        }
    }
}
