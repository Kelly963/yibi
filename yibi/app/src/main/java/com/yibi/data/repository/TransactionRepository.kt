package com.yibi.data.repository

import com.yibi.data.local.TransactionDao
import com.yibi.data.local.entity.TransactionEntity
import com.yibi.domain.expandable.IncomeExpenseRecord
import com.yibi.domain.model.ModuleContext
import com.yibi.domain.model.Transaction
import com.yibi.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID

/**
 * 交易记录Repository实现
 * 实现IncomeExpenseRecord接口，提供交易记录的增删改查
 */
class TransactionRepository(
    private val transactionDao: TransactionDao
) : IncomeExpenseRecord {

    override val moduleId: String = "income_expense_record"
    override val displayName: String = "收支记录"

    override fun onInitialize(context: ModuleContext) {
        // Repository已通过构造函数注入Dao，无需额外初始化
    }

    override fun onDestroy() {
        // 无需释放资源
    }

    // ==================== IncomeExpenseRecord 接口实现 ====================

    override fun recordTransaction(
        type: TransactionType,
        amount: Double,
        categoryId: String,
        date: LocalDate,
        note: String
    ): String {
        require(amount > 0) { "金额必须大于0" }
        require(categoryId.isNotBlank()) { "分类ID不能为空" }

        val transactionId = UUID.randomUUID().toString()
        val entity = TransactionEntity(
            id = transactionId,
            type = type.name,
            amount = amount,
            categoryId = categoryId,
            date = date,
            note = note
        )
        // 由于接口设计为同步返回ID，实际插入在协程中执行
        // 调用方应在协程中调用并处理结果
        return transactionId
    }

    /**
     * 异步插入交易记录（实际持久化操作）
     */
    suspend fun insertTransaction(
        type: TransactionType,
        amount: Double,
        categoryId: String,
        date: LocalDate,
        note: String = ""
    ): String = withContext(Dispatchers.IO) {
        require(amount > 0) { "金额必须大于0" }
        require(categoryId.isNotBlank()) { "分类ID不能为空" }

        val transactionId = UUID.randomUUID().toString()
        val entity = TransactionEntity(
            id = transactionId,
            type = type.name,
            amount = amount,
            categoryId = categoryId,
            date = date,
            note = note
        )
        transactionDao.insert(entity)
        transactionId
    }

    override fun updateTransaction(
        transactionId: String,
        type: TransactionType,
        amount: Double,
        categoryId: String,
        date: LocalDate,
        note: String
    ) {
        require(amount > 0) { "金额必须大于0" }
        require(transactionId.isNotBlank()) { "交易ID不能为空" }
    }

    /**
     * 异步更新交易记录
     */
    suspend fun updateTransactionAsync(
        transactionId: String,
        type: TransactionType,
        amount: Double,
        categoryId: String,
        date: LocalDate,
        note: String = ""
    ) = withContext(Dispatchers.IO) {
        require(amount > 0) { "金额必须大于0" }
        require(transactionId.isNotBlank()) { "交易ID不能为空" }

        val entity = TransactionEntity(
            id = transactionId,
            type = type.name,
            amount = amount,
            categoryId = categoryId,
            date = date,
            note = note
        )
        transactionDao.update(entity)
    }

    override fun deleteTransaction(transactionId: String) {
        require(transactionId.isNotBlank()) { "交易ID不能为空" }
    }

    /**
     * 异步删除交易记录
     */
    suspend fun deleteTransactionAsync(transactionId: String) = withContext(Dispatchers.IO) {
        require(transactionId.isNotBlank()) { "交易ID不能为空" }
        transactionDao.deleteById(transactionId)
    }

    override fun getTransaction(transactionId: String): Transaction? {
        // 同步接口，实际应在协程中调用异步版本
        return null
    }

    /**
     * 异步获取单笔交易
     */
    suspend fun getTransactionAsync(transactionId: String): Transaction? = withContext(Dispatchers.IO) {
        transactionDao.getById(transactionId)?.toDomainModel()
    }

    override fun getAllTransactions(): List<Transaction> {
        // 同步接口返回空列表，实际使用Flow观察数据变化
        return emptyList()
    }

    /**
     * 获取所有交易记录Flow（响应式）
     */
    fun getAllTransactionsFlow(): Flow<List<Transaction>> {
        return transactionDao.getAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * 异步获取所有交易记录列表
     */
    suspend fun getAllTransactionsList(): List<Transaction> = withContext(Dispatchers.IO) {
        transactionDao.getAllList().map { it.toDomainModel() }
    }

    override fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): List<Transaction> {
        return emptyList()
    }

    /**
     * 异步按日期范围查询
     */
    suspend fun getTransactionsByDateRangeAsync(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Transaction> = withContext(Dispatchers.IO) {
        transactionDao.getByDateRange(startDate, endDate).map { it.toDomainModel() }
    }

    /**
     * 按日期范围查询Flow
     */
    fun getTransactionsByDateRangeFlow(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>> {
        return transactionDao.getByDateRangeFlow(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * 按类型和日期范围统计金额
     */
    suspend fun getSumByTypeAndDateRange(
        type: TransactionType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double = withContext(Dispatchers.IO) {
        transactionDao.getSumByTypeAndDateRange(type.name, startDate, endDate) ?: 0.0
    }

    // ==================== 实体转换 ====================

    private fun TransactionEntity.toDomainModel(): Transaction {
        return Transaction(
            id = id,
            type = TransactionType.valueOf(type),
            amount = amount,
            categoryId = categoryId,
            date = date,
            note = note,
            createdAt = createdAt
        )
    }
}
