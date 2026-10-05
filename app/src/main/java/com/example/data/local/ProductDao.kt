package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode AND isActive = 1 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE isActive = 1 AND (name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND stock <= minStockAlert ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND stock <= 0 ORDER BY name ASC")
    fun getOutOfStockProducts(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    fun getActiveProductsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1 AND stock <= minStockAlert")
    fun getLowStockProductsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1 AND stock <= 0")
    fun getOutOfStockProductsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(costPrice * stock), 0.0) FROM products WHERE isActive = 1")
    fun getTotalInventoryCostValuation(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>): List<Long>

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
    suspend fun increaseStock(productId: Long, quantity: Double)

    @Query("UPDATE products SET stock = stock - :quantity WHERE id = :productId")
    suspend fun decreaseStock(productId: Long, quantity: Double)

    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun setStock(productId: Long, newStock: Double)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET isActive = 0 WHERE id = :productId")
    suspend fun softDeleteProduct(productId: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}
