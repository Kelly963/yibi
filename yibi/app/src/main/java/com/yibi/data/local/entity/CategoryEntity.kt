package com.yibi.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 分类数据库实体
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["type"]), Index(value = ["sortOrder"])]
)
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String,           // "INCOME" 或 "EXPENSE"
    val icon: String,
    val sortOrder: Int = 0,
    val isBuiltIn: Boolean = false,
    val isHidden: Boolean = false  // 软删除：隐藏后不显示在记账分类选择中
)
