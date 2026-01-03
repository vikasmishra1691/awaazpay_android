package com.example.awaazpay.parser

import com.example.awaazpay.util.Logger
import com.example.awaazpay.util.UpiAppMapper
import java.text.DecimalFormat

object PaymentParser {

    private val PAYMENT_KEYWORDS = listOf(
        "received", "credited", "paid", "payment successful",
        "payment received", "money received", "transaction successful",
        "deposited", "credit", "sent you", "transferred",
        "received from", "credited to", "account credited",
        "payment of", "rs.", "inr", "₹"
    )

    private val IGNORE_KEYWORDS = listOf(
        "otp", "one time password", "verification code",
        "failed", "decline", "unsuccessful", "rejected",
        "offer", "cashback", "scratch", "promocode"
    )

    private val AMOUNT_PATTERNS = listOf(
        Regex("""₹\s*(\d+(?:,\d+)*(?:\.\d{1,2})?)"""),  // ₹150, ₹1,500.00
        Regex("""Rs\.?\s*(\d+(?:,\d+)*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),  // Rs 150, Rs. 150
        Regex("""INR\s*(\d+(?:,\d+)*(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)  // INR 150
    )

    private val SENDER_PATTERNS = listOf(
        Regex("""from\s+([A-Za-z\s]+?)(?:\s+on|\s+via|\s+to|$)""", RegexOption.IGNORE_CASE),
        Regex("""by\s+([A-Za-z\s]+?)(?:\s+on|\s+via|\s+to|$)""", RegexOption.IGNORE_CASE),
        Regex("""([A-Za-z\s]+?)\s+sent you""", RegexOption.IGNORE_CASE),
        Regex("""([A-Za-z\s]+?)\s+paid you""", RegexOption.IGNORE_CASE)
    )

    fun parsePayment(text: String?, appName: String): ParsedPayment? {
        if (text.isNullOrBlank()) {
            Logger.d("Payment parsing failed: text is null or blank")
            return null
        }

        Logger.d("========== PAYMENT PARSING START ==========")
        Logger.d("Parsing notification text: $text")
        Logger.d("App package name: $appName")

        // Check if this is an ignorable notification
        if (shouldIgnore(text)) {
            Logger.d("Notification ignored due to ignore keywords")
            Logger.d("========== PAYMENT PARSING END (IGNORED) ==========")
            return null
        }

        // Check if this contains payment keywords
        if (!hasPaymentKeyword(text)) {
            Logger.d("Notification rejected: no payment keywords found")
            Logger.d("Text lowercase: ${text.lowercase()}")
            Logger.d("========== PAYMENT PARSING END (NO KEYWORDS) ==========")
            return null
        }

        Logger.d("Payment keyword found!")

        // Extract amount
        val amount = extractAmount(text)
        if (amount == null) {
            Logger.d("Payment parsing failed: no amount found")
            Logger.d("========== PAYMENT PARSING END (NO AMOUNT) ==========")
            return null
        }

        Logger.d("Amount extracted: $amount")

        // Extract sender name
        val senderName = extractSenderName(text)
        Logger.d("Sender name extracted: $senderName")

        // Convert package name to user-friendly display name
        val displayName = UpiAppMapper.getDisplayName(appName)
        Logger.d("App display name: $displayName")

        Logger.d("Payment parsed successfully: amount=$amount, sender=$senderName, app=$displayName")
        Logger.d("========== PAYMENT PARSING END (SUCCESS) ==========")
        return ParsedPayment(amount, senderName, displayName)
    }

    private fun shouldIgnore(text: String): Boolean {
        val lowerText = text.lowercase()
        return IGNORE_KEYWORDS.any { lowerText.contains(it) }
    }

    private fun hasPaymentKeyword(text: String): Boolean {
        val lowerText = text.lowercase()
        return PAYMENT_KEYWORDS.any { lowerText.contains(it) }
    }

    private fun extractAmount(text: String): String? {
        for (pattern in AMOUNT_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) {
                val amountStr = match.groupValues[1].replace(",", "")
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    // Format to always have 2 decimal places
                    val formatter = DecimalFormat("0.00")
                    return formatter.format(amount)
                }
            }
        }
        return null
    }

    private fun extractSenderName(text: String): String? {
        for (pattern in SENDER_PATTERNS) {
            val match = pattern.find(text)
            if (match != null) {
                val name = match.groupValues[1].trim()
                // Validate name (should be alphabetic with possible spaces, 2-50 chars)
                if (name.length in 2..50 && name.matches(Regex("[A-Za-z\\s]+"))) {
                    return name
                }
            }
        }
        return null
    }
}

