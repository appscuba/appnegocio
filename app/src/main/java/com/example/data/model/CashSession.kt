package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_sessions")
data class CashSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val openTimestamp: Long = System.currentTimeMillis(),
    val closeTimestamp: Long? = null,
    val initialCash: Double, // Fondo de caja al abrir
    val expectedCash: Double = initialCash, // Calculado por el sistema
    val actualCash: Double? = null, // Efectivo contado por el usuario al cerrar
    val difference: Double? = null, // actualCash - expectedCash (positivo: sobrante, negativo: faltante)
    val status: String = "OPEN", // OPEN, CLOSED
    val notes: String = ""
)

@Entity(tableName = "cash_movements")
data class CashMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val type: String, // INGRESO (Entrada dinero), RETIRO (Salida dinero), GASTO, VENTA_EFECTIVO
    val amount: Double,
    val category: String = "General",
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
