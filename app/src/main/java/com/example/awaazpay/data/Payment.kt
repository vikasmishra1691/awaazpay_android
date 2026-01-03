package com.example.awaazpay.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: String,
    val senderName: String?,
    val appName: String,
    val timestamp: Long,
    val rawNotificationText: String
)

