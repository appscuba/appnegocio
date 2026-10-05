package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String = "",
    val name: String,
    val category: String = "General",
    val costPrice: Double, // Precio de compra al proveedor
    val sellingPrice: Double, // Precio de venta al público
    val stock: Double, // Cantidad en inventario
    val minStockAlert: Double = 5.0, // Umbral para avisar stock bajo
    val unit: String = "pza", // pza, kg, lt, caja, paquete, etc.
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val profitPerUnit: Double get() = sellingPrice - costPrice
    val profitMarginPercent: Double
        get() = if (costPrice > 0) ((sellingPrice - costPrice) / costPrice) * 100 else 0.0
    val isLowStock: Boolean get() = stock > 0 && stock <= minStockAlert
    val isOutOfStock: Boolean get() = stock <= 0
}
