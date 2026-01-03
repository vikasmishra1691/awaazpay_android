package com.example.awaazpay.util

import android.util.Log
import com.example.awaazpay.BuildConfig

object Logger {
    private const val TAG = "AwaazPay"

    fun d(message: String) {
        if (BuildConfig.DEBUG_MODE) {
            Log.d(TAG, message)
        }
    }

    fun i(message: String) {
        // Always log info messages for critical events
        Log.i(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        // Always log errors
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }

    fun w(message: String) {
        if (BuildConfig.DEBUG_MODE) {
            Log.w(TAG, message)
        }
    }
}

