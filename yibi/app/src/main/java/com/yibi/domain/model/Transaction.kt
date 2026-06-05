package com.yibi.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 交易记录领域模型
 */
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val categoryId: String,
    val date: LocalDate,
    val note: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
