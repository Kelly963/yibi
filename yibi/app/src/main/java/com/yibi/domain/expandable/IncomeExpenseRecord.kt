package com.yibi.domain.expandable

import com.yibi.domain.model.ModuleContext
import com.yibi.domain.model.TransactionType
import java.time.LocalDate

/**
 * 收支记录扩展接口，定义交易录入的扩展点。
 */
interface IncomeExpenseRecord : ExpandableUnit {
    override val moduleId: String get() = "income_expense_record"
    override val displayName: String get() = "收支记录"

    override fun onInitialize(context: ModuleContext) {
        // 初始化数据库连接及分类缓存
    }

    override fun onDestroy() {
        // 释放数据库连接
    }

    /**
     * 记录一笔交易，返回交易ID。
     * @param type 收入或支出
     * @param amount 交易金额，单位：元
     * @param categoryId 分类ID
     * @param date 交易日期
     * @param note 备注（可选）
     * @return 交易唯一标识
     */
    fun recordTransaction(
        type: TransactionType,
        amount: Double,
        categoryId: String,
        date: LocalDate,
        note: String = ""
    ): String

    /**
     * 更新指定交易。
     */
    fun updateTransaction(
        transactionId: String,
        type: TransactionType,
        amount: Double,
        categoryId: String,
        date: LocalDate,
        note: String = ""
    )

    /**
     * 删除指定交易。
     */
    fun deleteTransaction(transactionId: String)

    /**
     * 获取单笔交易详情。
     */
    fun getTransaction(transactionId: String): com.yibi.domain.model.Transaction?

    /**
     * 获取所有交易记录（按日期降序）。
     */
    fun getAllTransactions(): List<com.yibi.domain.model.Transaction>

    /**
     * 按日期范围查询交易记录。
     */
    fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): List<com.yibi.domain.model.Transaction>
}
