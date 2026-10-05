package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.SaleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Transaction
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSalesWithItems(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :saleId LIMIT 1")
    suspend fun getSaleWithItemsById(saleId: Long): SaleWithItems?

    @Transaction
    @Query("SELECT * FROM sales WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp ORDER BY timestamp DESC")
    fun getSalesBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<List<SaleWithItems>>

    @Query("SELECT COUNT(*) FROM sales WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp AND status = 'COMPLETED'")
    fun getSalesCountBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM sales WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp AND status = 'COMPLETED'")
    fun getTotalSalesAmountBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(profit), 0.0) FROM sales WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp AND status = 'COMPLETED'")
    fun getTotalProfitBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM sales WHERE paymentMethod = 'Efectivo' AND timestamp >= :startTimestamp AND timestamp <= :endTimestamp AND status = 'COMPLETED'")
    suspend fun getCashSalesSum(startTimestamp: Long, endTimestamp: Long): Double

    @Query("SELECT * FROM sale_items")
    suspend fun getAllSaleItems(): List<SaleItem>

    @Query("SELECT si.* FROM sale_items si INNER JOIN sales s ON si.saleId = s.id WHERE s.timestamp >= :startTimestamp AND s.timestamp <= :endTimestamp AND s.status = 'COMPLETED'")
    suspend fun getSaleItemsBetweenDates(startTimestamp: Long, endTimestamp: Long): List<SaleItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>): List<Long>

    @Query("UPDATE sales SET status = 'CANCELLED' WHERE id = :saleId")
    suspend fun cancelSale(saleId: Long)

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()

    @Query("DELETE FROM sale_items")
    suspend fun deleteAllSaleItems()
}
