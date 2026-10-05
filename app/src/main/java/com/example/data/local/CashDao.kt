package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CashMovement
import com.example.data.model.CashSession
import kotlinx.coroutines.flow.Flow

@Dao
interface CashDao {
    @Query("SELECT * FROM cash_sessions WHERE status = 'OPEN' ORDER BY id DESC LIMIT 1")
    fun getCurrentOpenSessionFlow(): Flow<CashSession?>

    @Query("SELECT * FROM cash_sessions WHERE status = 'OPEN' ORDER BY id DESC LIMIT 1")
    suspend fun getCurrentOpenSession(): CashSession?

    @Query("SELECT * FROM cash_sessions ORDER BY openTimestamp DESC")
    fun getAllSessions(): Flow<List<CashSession>>

    @Query("SELECT * FROM cash_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): CashSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CashSession): Long

    @Update
    suspend fun updateSession(session: CashSession)

    @Query("SELECT * FROM cash_movements WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getMovementsForSession(sessionId: Long): Flow<List<CashMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: CashMovement): Long

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cash_movements WHERE sessionId = :sessionId AND type IN ('INGRESO', 'VENTA_EFECTIVO')")
    suspend fun getSessionCashInSum(sessionId: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM cash_movements WHERE sessionId = :sessionId AND type IN ('RETIRO', 'GASTO')")
    suspend fun getSessionCashOutSum(sessionId: Long): Double

    @Query("DELETE FROM cash_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM cash_movements")
    suspend fun deleteAllMovements()
}
