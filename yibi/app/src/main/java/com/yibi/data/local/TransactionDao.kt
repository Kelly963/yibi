package com.yibi.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yibi.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 交易记录数据访问对象
 */
@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteById(transactionId: String)

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getById(transactionId: String): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    suspend fun getAllList(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRangeFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getSumByTypeAndDateRange(type: String, startDate: LocalDate, endDate: LocalDate): Double?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getCount(): Int
}
