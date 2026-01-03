package com.example.awaazpay.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.awaazpay.service.PaymentAnnouncementService
import com.example.awaazpay.util.Logger

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Logger.d("Device booted - BootReceiver triggered")

            context?.let {
                val prefs = it.getSharedPreferences("awaazpay_prefs", Context.MODE_PRIVATE)
                val autoStartEnabled = prefs.getBoolean("auto_start_on_boot", false)

                if (autoStartEnabled) {
                    Logger.d("Auto-start enabled - starting announcement service")
                    PaymentAnnouncementService.startService(it)
                } else {
                    Logger.d("Auto-start disabled")
                }
            }
        }
    }
}

