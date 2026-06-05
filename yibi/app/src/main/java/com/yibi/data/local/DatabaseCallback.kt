package com.yibi.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yibi.data.local.entity.CategoryEntity
import com.yibi.domain.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 数据库创建回调：预置基础分类数据
 */
class DatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // 预置数据在AppDatabase构建后通过Repository初始化
    }

    companion object {
        /**
         * 预置分类数据
         */
        fun getBuiltInCategories(): List<CategoryEntity> = listOf(
            // 支出分类（更丰富）
            CategoryEntity(UUID.randomUUID().toString(), "餐饮", TransactionType.EXPENSE.name, "restaurant", 1, true),
            CategoryEntity(UUID.randomUUID().toString(), "交通", TransactionType.EXPENSE.name, "directions_car", 2, true),
            CategoryEntity(UUID.randomUUID().toString(), "购物", TransactionType.EXPENSE.name, "shopping_cart", 3, true),
            CategoryEntity(UUID.randomUUID().toString(), "娱乐", TransactionType.EXPENSE.name, "sports_esports", 4, true),
            CategoryEntity(UUID.randomUUID().toString(), "居住", TransactionType.EXPENSE.name, "home", 5, true),
            CategoryEntity(UUID.randomUUID().toString(), "医疗", TransactionType.EXPENSE.name, "local_hospital", 6, true),
            CategoryEntity(UUID.randomUUID().toString(), "教育", TransactionType.EXPENSE.name, "school", 7, true),
            CategoryEntity(UUID.randomUUID().toString(), "通讯", TransactionType.EXPENSE.name, "phone_android", 8, true),
            CategoryEntity(UUID.randomUUID().toString(), "服饰", TransactionType.EXPENSE.name, "checkroom", 9, true),
            CategoryEntity(UUID.randomUUID().toString(), "美妆", TransactionType.EXPENSE.name, "face", 10, true),
            CategoryEntity(UUID.randomUUID().toString(), "宠物", TransactionType.EXPENSE.name, "pets", 11, true),
            CategoryEntity(UUID.randomUUID().toString(), "旅行", TransactionType.EXPENSE.name, "flight", 12, true),
            CategoryEntity(UUID.randomUUID().toString(), "数码", TransactionType.EXPENSE.name, "phone_android", 13, true),
            CategoryEntity(UUID.randomUUID().toString(), "运动", TransactionType.EXPENSE.name, "fitness_center", 14, true),
            CategoryEntity(UUID.randomUUID().toString(), "零食饮料", TransactionType.EXPENSE.name, "local_cafe", 15, true),
            CategoryEntity(UUID.randomUUID().toString(), "人情往来", TransactionType.EXPENSE.name, "volunteer_activism", 16, true),
            CategoryEntity(UUID.randomUUID().toString(), "其他支出", TransactionType.EXPENSE.name, "more_horiz", 17, true),
            // 收入分类
            CategoryEntity(UUID.randomUUID().toString(), "工资", TransactionType.INCOME.name, "account_balance_wallet", 101, true),
            CategoryEntity(UUID.randomUUID().toString(), "奖金", TransactionType.INCOME.name, "card_giftcard", 102, true),
            CategoryEntity(UUID.randomUUID().toString(), "投资收益", TransactionType.INCOME.name, "trending_up", 103, true),
            CategoryEntity(UUID.randomUUID().toString(), "兼职", TransactionType.INCOME.name, "work", 104, true),
            CategoryEntity(UUID.randomUUID().toString(), "红包", TransactionType.INCOME.name, "redeem", 105, true),
            CategoryEntity(UUID.randomUUID().toString(), "退款", TransactionType.INCOME.name, "redeem", 106, true),
            CategoryEntity(UUID.randomUUID().toString(), "其他收入", TransactionType.INCOME.name, "more_horiz", 107, true)
        )
    }
}
