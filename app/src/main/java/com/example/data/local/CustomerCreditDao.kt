package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerCredit
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerCreditDao {
    @Query("SELECT * FROM customer_credits ORDER BY isPaid ASC, createdAt DESC")
    fun getAllCredits(): Flow<List<CustomerCredit>>

    @Query("SELECT * FROM customer_credits WHERE isPaid = 0 ORDER BY createdAt DESC")
    fun getPendingCredits(): Flow<List<CustomerCredit>>

    @Query("SELECT COUNT(*) FROM customer_credits WHERE isPaid = 0")
    fun getPendingCreditsCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalDebt - paidAmount), 0.0) FROM customer_credits WHERE isPaid = 0")
    fun getTotalPendingCreditsAmount(): Flow<Double>

    @Query("SELECT * FROM customer_credits WHERE id = :id LIMIT 1")
    suspend fun getCreditById(id: Long): CustomerCredit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredit(credit: CustomerCredit): Long

    @Update
    suspend fun updateCredit(credit: CustomerCredit)

    @Delete
    suspend fun deleteCredit(credit: CustomerCredit)

    @Query("DELETE FROM customer_credits")
    suspend fun deleteAllCredits()
}
