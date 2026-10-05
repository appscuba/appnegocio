package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val totalCost: Double,
    val profit: Double,
    val paymentMethod: String = "Efectivo", // Efectivo, Tarjeta, Transferencia, Fiado
    val amountReceived: Double = 0.0,
    val changeGiven: Double = 0.0,
    val customerName: String = "Cliente Mostrador",
    val customerPhone: String = "",
    val notes: String = "",
    val isCreditSale: Boolean = false,
    val status: String = "COMPLETED" // COMPLETED, CANCELLED
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["saleId"])]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double,
    val unitCost: Double,
    val unitPrice: Double,
    val subtotal: Double,
    val profit: Double
)

data class SaleWithItems(
    @Embedded val sale: Sale,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItem>
)
