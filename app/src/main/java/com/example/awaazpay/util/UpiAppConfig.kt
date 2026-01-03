package com.example.awaazpay.util

object UpiAppConfig {
    val SUPPORTED_UPI_APPS = mapOf(
        "com.phonepe.app" to "PhonePe",
        "com.google.android.apps.nbu.paisa.user" to "Google Pay",
        "net.one97.paytm" to "Paytm",
        "in.org.npci.upiapp" to "BHIM",
        "in.amazon.mShop.android.shopping" to "Amazon Pay",
        "com.whatsapp" to "WhatsApp Pay"
    )

    fun isSupportedApp(packageName: String?): Boolean {
        return packageName != null && SUPPORTED_UPI_APPS.containsKey(packageName)
    }

    fun getAppName(packageName: String?): String {
        return SUPPORTED_UPI_APPS[packageName] ?: "Unknown"
    }
}

