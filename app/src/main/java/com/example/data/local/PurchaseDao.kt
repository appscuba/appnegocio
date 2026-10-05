package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.Purchase
import com.example.data.model.PurchaseItem
import com.example.data.model.PurchaseWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Transaction
    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchasesWithItems(): Flow<List<PurchaseWithItems>>

    @Transaction
    @Query("SELECT * FROM purchases WHERE id = :purchaseId LIMIT 1")
    suspend fun getPurchaseById(purchaseId: Long): PurchaseWithItems?

    @Transaction
    @Query("SELECT * FROM purchases WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp ORDER BY timestamp DESC")
    fun getPurchasesBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<List<PurchaseWithItems>>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM purchases WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp")
    fun getTotalPurchasesAmountBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItem>): List<Long>

    @Query("DELETE FROM purchases")
    suspend fun deleteAllPurchases()

    @Query("DELETE FROM purchase_items")
    suspend fun deleteAllPurchaseItems()
}
