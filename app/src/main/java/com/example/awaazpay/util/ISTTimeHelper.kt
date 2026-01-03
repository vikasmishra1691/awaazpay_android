package com.example.awaazpay.util

import java.util.Calendar
import java.util.TimeZone

object ISTTimeHelper {
    private val IST_TIMEZONE = TimeZone.getTimeZone("Asia/Kolkata")

    fun getCurrentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance(IST_TIMEZONE)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val result = calendar.timeInMillis
        Logger.d("Start of today: ${formatTimestamp(result)}")
        return result
    }

    fun getStartOfWeek(): Long {
        val calendar = Calendar.getInstance(IST_TIMEZONE)

        // Get current day of week (1=Sunday, 7=Saturday)
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Calculate days to subtract to get to Sunday
        val daysFromSunday = currentDayOfWeek - Calendar.SUNDAY

        Logger.d("Current day of week: $currentDayOfWeek (1=Sunday, 7=Saturday)")
        Logger.d("Days from Sunday: $daysFromSunday")

        // Go back to this week's Sunday
        calendar.add(Calendar.DAY_OF_MONTH, -daysFromSunday)

        // Set to start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val result = calendar.timeInMillis
        Logger.d("Start of week: ${formatTimestamp(result)}")
        return result
    }

    fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance(IST_TIMEZONE)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val result = calendar.timeInMillis
        Logger.d("Start of month: ${formatTimestamp(result)}")
        return result
    }

    fun formatTimestamp(timestamp: Long): String {
        val calendar = Calendar.getInstance(IST_TIMEZONE)
        calendar.timeInMillis = timestamp
        return String.format(
            java.util.Locale.US,
            "%02d:%02d:%02d %02d/%02d/%d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )
    }
}

