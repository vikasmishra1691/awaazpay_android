package com.example.awaazpay.parser

data class ParsedPayment(
    val amount: String,
    val senderName: String?,
    val appName: String
)

