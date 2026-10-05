package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_credits")
data class CustomerCredit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val customerPhone: String = "",
    val saleId: Long? = null,
    val totalDebt: Double,
    val paidAmount: Double = 0.0,
    val isPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val lastPaymentDate: Long? = null,
    val notes: String = ""
) {
    val remainingBalance: Double get() = (totalDebt - paidAmount).coerceAtLeast(0.0)
}
