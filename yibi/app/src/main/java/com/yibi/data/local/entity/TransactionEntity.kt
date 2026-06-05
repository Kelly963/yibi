package com.yibi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 交易记录数据库实体
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["categoryId"]), Index(value = ["date"])]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val type: String,           // "INCOME" 或 "EXPENSE"
    val amount: Double,
    val categoryId: String,
    val date: LocalDate,
    val note: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
