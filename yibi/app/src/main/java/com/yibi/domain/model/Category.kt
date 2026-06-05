package com.yibi.domain.model

/**
 * 分类领域模型
 */
data class Category(
    val id: String,
    val name: String,
    val type: TransactionType,
    val icon: String,
    val sortOrder: Int = 0,
    val isBuiltIn: Boolean = false,
    val isHidden: Boolean = false
)
