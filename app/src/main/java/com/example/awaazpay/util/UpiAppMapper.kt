package com.example.awaazpay.util

/**
 * Maps UPI app package names to user-friendly display names
 */
object UpiAppMapper {

    private val packageToDisplayName = mapOf(
        // Major UPI Apps
        "com.google.android.apps.nbu.paisa.user" to "Google Pay",
        "com.phonepe.app" to "PhonePe",
        "net.one97.paytm" to "Paytm",
        "in.org.npci.upiapp" to "BHIM UPI",

        // E-commerce & Wallets
        "com.amazon.mShop.android.shopping" to "Amazon Pay",
        "com.freecharge.android" to "Freecharge",
        "com.mobikwik_new" to "MobiKwik",

        // Social & Messaging
        "com.whatsapp" to "WhatsApp Pay",

        // Business & Merchant Apps
        "com.bharatpe.merchant.user" to "BharatPe Merchant",
        "in.co.bharatpe" to "BharatPe",

        // Credit & Lending
        "com.dreamplug.androidapp" to "CRED",

        // Telecom Payment Apps
        "com.myairtelapp" to "Airtel Thanks",
        "com.airtel.money" to "Airtel Money",

        // Bank UPI Apps
        "com.csam.icici.bank.imobile" to "iMobile Pay",
        "com.axis.mobile" to "Axis Mobile",
        "com.sbi.lotusintouch" to "YONO SBI",
        "com.sbi.SBIFreedomPlus" to "YONO Lite",
        "com.snapwork.hdfc" to "HDFC Bank",
        "com.fedbank.fednxt" to "Federal Bank",
        "com.fss.pnb.mbanking" to "PNB One"
    )

    /**
     * Converts a package name to a user-friendly display name
     * @param packageName The Android package name (e.g., "com.phonepe.app")
     * @return User-friendly display name (e.g., "PhonePe"), or the package name if not found
     */
    fun getDisplayName(packageName: String): String {
        return packageToDisplayName[packageName] ?: packageName
    }

    /**
     * Checks if a package name is a known UPI app
     * @param packageName The Android package name
     * @return true if the package is a recognized UPI app
     */
    fun isKnownUpiApp(packageName: String): Boolean {
        return packageToDisplayName.containsKey(packageName)
    }
}

