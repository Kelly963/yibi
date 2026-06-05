package com.yibi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibi.data.repository.CategoryRepository
import com.yibi.data.repository.TransactionRepository
import com.yibi.domain.model.Category
import com.yibi.domain.model.Transaction
import com.yibi.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 首页ViewModel
 */
class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // 所有交易记录
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    // 所有分类
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    // 今日收支统计
    private val _todayIncome = MutableStateFlow(0.0)
    val todayIncome: StateFlow<Double> = _todayIncome

    private val _todayExpense = MutableStateFlow(0.0)
    val todayExpense: StateFlow<Double> = _todayExpense

    // 本月收支统计
    private val _monthIncome = MutableStateFlow(0.0)
    val monthIncome: StateFlow<Double> = _monthIncome

    private val _monthExpense = MutableStateFlow(0.0)
    val monthExpense: StateFlow<Double> = _monthExpense

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // FAB 可拖动位置
    private val _fabOffset = MutableStateFlow(Pair(0f, 0f))
    val fabOffset: StateFlow<Pair<Float, Float>> = _fabOffset

    fun updateFabOffset(x: Float, y: Float) {
        _fabOffset.value = Pair(x, y)
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 初始化预置分类
                categoryRepository.initializeBuiltInCategories()

                // 收集交易记录
                transactionRepository.getAllTransactionsFlow().collect { transList ->
                    _transactions.value = transList
                    updateStatistics(transList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }

        viewModelScope.launch {
            categoryRepository.getAllCategoriesFlow().collect { catList ->
                _categories.value = catList
            }
        }
    }

    private fun updateStatistics(transList: List<Transaction>) {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())

        // 今日统计
        _todayIncome.value = transList
            .filter { it.type == TransactionType.INCOME && it.date == today }
            .sumOf { it.amount }

        _todayExpense.value = transList
            .filter { it.type == TransactionType.EXPENSE && it.date == today }
            .sumOf { it.amount }

        // 本月统计
        _monthIncome.value = transList
            .filter { it.type == TransactionType.INCOME && it.date in monthStart..monthEnd }
            .sumOf { it.amount }

        _monthExpense.value = transList
            .filter { it.type == TransactionType.EXPENSE && it.date in monthStart..monthEnd }
            .sumOf { it.amount }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransactionAsync(transactionId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getCategoryName(categoryId: String): String {
        return _categories.value.find { it.id == categoryId }?.name ?: "未知分类"
    }

    fun getCategoryIcon(categoryId: String): String {
        return _categories.value.find { it.id == categoryId }?.icon ?: "more_horiz"
    }
}
