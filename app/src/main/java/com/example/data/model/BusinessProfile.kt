package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "Mi Tienda / Negocio",
    val ownerName: String = "Administrador",
    val taxId: String = "RFC / RUT / CIF",
    val phone: String = "+52 55 1234 5678",
    val address: String = "Calle Principal #123, Centro",
    val currencySymbol: String = "$",
    val receiptHeader: String = "¡Gracias por su compra!",
    val receiptFooter: String = "Revise su mercancía antes de retirarse. No se aceptan devoluciones después de 7 días.",
    val lowStockThresholdDefault: Double = 5.0
)
