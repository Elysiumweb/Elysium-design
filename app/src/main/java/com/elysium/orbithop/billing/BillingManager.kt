package com.elysium.orbithop.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elysium.orbithop.core.store.FirestoreRepository
import com.elysium.orbithop.core.store.LocalStore
import com.elysium.orbithop.core.util.Analytics
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Product
import com.android.billingclient.api.ProductType
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.launch

/**
 * Achat intégré « Supprimer les pubs » (F09) + restauration (F10).
 * Produit Play Console attendu : produit unique « remove_ads » (type : produit unique / INAPP).
 * Les vidéos récompensées restent disponibles même après l'achat (choix du joueur).
 */
object BillingManager {

    private const val TAG = "Billing"
    const val PRODUCT_REMOVE_ADS = "remove_ads"

    private var client: BillingClient? = null
    private var productDetails: com.android.billingclient.api.ProductDetails? = null
    private val appScope = kotlinx.coroutines.MainScope()

    var purchaseInFlight by mutableStateOf(false)
        private set

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        purchaseInFlight = false
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "Achat annulé par l'utilisateur")
        } else {
            Log.w(TAG, "Erreur achat: ${result.debugMessage}")
        }
    }

    fun init(context: Context) {
        if (client != null) return
        client = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()
        startConnection(context.applicationContext)
    }

    fun end() {
        runCatching { client?.endConnection() }
        client = null
    }

    private fun startConnection(context: Context) {
        client?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProduct(context)
                    restorePurchases(context)
                } else {
                    Log.w(TAG, "Billing setup: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Reconnexion différée simple
                Log.i(TAG, "Billing déconnecté")
            }
        })
    }

    private fun queryProduct(context: Context) {
        val c = client ?: return
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    Product.newBuilder()
                        .setProductId(PRODUCT_REMOVE_ADS)
                        .setProductType(ProductType.INAPP)
                        .build()
                )
            )
            .build()
        c.queryProductDetailsAsync(params) { billingResult, result ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = result.productDetailsList.firstOrNull()
            }
            Log.i(TAG, "Produit trouvé: ${productDetails != null}")
        }
    }

    /** Restaure « Supprimer les pubs » sur un nouvel appareil (F10). */
    fun restorePurchases(context: Context) {
        val c = client ?: return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        c.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val owned = purchases.any {
                    it.products.contains(PRODUCT_REMOVE_ADS) &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (owned && !LocalStore.removeAds) {
                    LocalStore.setRemoveAds(true)
                    Analytics.log("iap_restored")
                }
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        acknowledge(purchase)
                    }
                }
            }
        }
    }

    /** Lance le flux d'achat depuis un écran (bouton « Supprimer les pubs »). */
    fun purchase(activity: Activity, onMessage: (String) -> Unit = {}) {
        if (LocalStore.removeAds) return
        val details = productDetails
        if (details == null) {
            onMessage("PRODUCT_UNAVAILABLE")
            return
        }
        val flow = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            )
            .build()
        purchaseInFlight = true
        val result = client?.launchBillingFlow(activity, flow)
        if (result?.responseCode != BillingClient.BillingResponseCode.OK) {
            purchaseInFlight = false
            onMessage(result?.debugMessage ?: "BILLING_UNAVAILABLE")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (purchase.products.contains(PRODUCT_REMOVE_ADS)) {
                LocalStore.setRemoveAds(true)
                Analytics.log("iap_remove_ads")
                val uid = com.elysium.orbithop.core.auth.AuthRepository.currentUid
                if (uid != null) {
                    // La synchro Firestore suit (fire-and-forget)
                    appScope.launch {
                        FirestoreRepository.pushProfile(uid)
                    }
                }
            }
            if (!purchase.isAcknowledged) {
                acknowledge(purchase)
            }
        }
    }

    private fun acknowledge(purchase: Purchase) {
        val c = client ?: return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        c.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "Acknowledge échoué: ${result.debugMessage}")
            }
        }
    }
}
