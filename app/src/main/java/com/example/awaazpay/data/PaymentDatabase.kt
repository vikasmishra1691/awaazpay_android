package com.example.awaazpay.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Payment::class], version = 1, exportSchema = false)
abstract class PaymentDatabase : RoomDatabase() {
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: PaymentDatabase? = null

        fun getDatabase(context: Context): PaymentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PaymentDatabase::class.java,
                    "payment_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

