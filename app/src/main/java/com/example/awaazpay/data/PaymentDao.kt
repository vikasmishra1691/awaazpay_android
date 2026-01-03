package com.example.awaazpay.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: Payment): Long

    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY timestamp DESC LIMIT 10")
    fun getRecentPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY timestamp DESC LIMIT 50")
    fun getLast50Payments(): Flow<List<Payment>>

    @Query("SELECT SUM(CAST(amount AS REAL)) FROM payments WHERE timestamp >= :startTime")
    fun getTotalEarningsFrom(startTime: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM payments")
    fun getTotalCount(): Flow<Int>

    @Query("DELETE FROM payments")
    suspend fun deleteAll()
}

