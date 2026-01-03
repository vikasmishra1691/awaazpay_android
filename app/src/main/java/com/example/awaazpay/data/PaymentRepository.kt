package com.example.awaazpay.data

import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val paymentDao: PaymentDao) {

    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val recentPayments: Flow<List<Payment>> = paymentDao.getRecentPayments()
    val last50Payments: Flow<List<Payment>> = paymentDao.getLast50Payments()

    suspend fun insert(payment: Payment): Long {
        return paymentDao.insert(payment)
    }

    fun getTotalEarningsFrom(startTime: Long): Flow<Double?> {
        return paymentDao.getTotalEarningsFrom(startTime)
    }

    fun getTotalCount(): Flow<Int> {
        return paymentDao.getTotalCount()
    }

    suspend fun deleteAll() {
        paymentDao.deleteAll()
    }
}

