package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // Alquiler, Servicios, Sueldos, Transporte, Mantenimiento, Mercadería, Otros
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val paymentMethod: String = "Efectivo",
    val receiptNumber: String = ""
)
