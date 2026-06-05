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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * 统计页面ViewModel
 */
class StatsViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // 时间维度：日、周、月
    enum class TimeRange {
        DAY, WEEK, MONTH
    }

    // 当前选中维度
    private val _timeRange = MutableStateFlow(TimeRange.MONTH)
    val timeRange: StateFlow<TimeRange> = _timeRange

    // 当前日期（用于计算范围）
    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate

    // 交易记录
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())

    // 分类数据
    private val _categories = MutableStateFlow<List<Category>>(emptyList())

    // 支出分类占比数据
    private val _expenseByCategory = MutableStateFlow<List<CategoryStat>>(emptyList())
    val expenseByCategory: StateFlow<List<CategoryStat>> = _expenseByCategory

    // 收支趋势数据（按日）
    private val _trendData = MutableStateFlow<List<DailyStat>>(emptyList())
    val trendData: StateFlow<List<DailyStat>> = _trendData

    // 支出排行
    private val _expenseRanking = MutableStateFlow<List<CategoryStat>>(emptyList())
    val expenseRanking: StateFlow<List<CategoryStat>> = _expenseRanking

    // 总收支
    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategoriesFlow().collect {
                _categories.value = it
                recalculate()
            }
        }
        viewModelScope.launch {
            transactionRepository.getAllTransactionsFlow().collect {
                _transactions.value = it
                recalculate()
            }
        }
    }

    fun setTimeRange(range: TimeRange) {
        _timeRange.value = range
        recalculate()
    }

    fun setCurrentDate(date: LocalDate) {
        _currentDate.value = date
        recalculate()
    }

    fun goToPreviousPeriod() {
        val current = _currentDate.value
        val newDate = when (_timeRange.value) {
            TimeRange.DAY -> current.minusDays(1)
            TimeRange.WEEK -> current.minusWeeks(1)
            TimeRange.MONTH -> current.minusMonths(1)
        }
        _currentDate.value = newDate
        recalculate()
    }

    fun goToNextPeriod() {
        val current = _currentDate.value
        val newDate = when (_timeRange.value) {
            TimeRange.DAY -> current.plusDays(1)
            TimeRange.WEEK -> current.plusWeeks(1)
            TimeRange.MONTH -> current.plusMonths(1)
        }
        if (newDate <= LocalDate.now()) {
            _currentDate.value = newDate
            recalculate()
        }
    }

    private fun recalculate() {
        val range = _timeRange.value
        val current = _currentDate.value

        val (startDate, endDate) = when (range) {
            TimeRange.DAY -> current to current
            TimeRange.WEEK -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val start = current.with(weekFields.dayOfWeek(), 1)
                val end = current.with(weekFields.dayOfWeek(), 7)
                start to end
            }
            TimeRange.MONTH -> {
                val start = current.withDayOfMonth(1)
                val end = current.withDayOfMonth(current.lengthOfMonth())
                start to end
            }
        }

        val filtered = _transactions.value.filter { it.date in startDate..endDate }

        // 总收支
        _totalIncome.value = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        _totalExpense.value = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        // 按分类统计支出
        val expenseList = filtered.filter { it.type == TransactionType.EXPENSE }
        val categoryMap = _categories.value.associateBy { it.id }

        val categoryStats = expenseList
            .groupBy { it.categoryId }
            .map { (categoryId, transList) ->
                val category = categoryMap[categoryId]
                CategoryStat(
                    categoryId = categoryId,
                    categoryName = category?.name ?: "未知分类",
                    icon = category?.icon ?: "more_horiz",
                    amount = transList.sumOf { it.amount },
                    count = transList.size
                )
            }
            .sortedByDescending { it.amount }

        _expenseByCategory.value = categoryStats
        _expenseRanking.value = categoryStats

        // 趋势数据（按日聚合）
        val dailyStats = (0..endDate.toEpochDay() - startDate.toEpochDay()).map { offset ->
            val date = startDate.plusDays(offset)
            val dayTrans = filtered.filter { it.date == date }
            DailyStat(
                date = date,
                income = dayTrans.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                expense = dayTrans.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            )
        }
        _trendData.value = dailyStats
    }

    /**
     * 分类统计项
     */
    data class CategoryStat(
        val categoryId: String,
        val categoryName: String,
        val icon: String,
        val amount: Double,
        val count: Int
    )

    /**
     * 每日统计项
     */
    data class DailyStat(
        val date: LocalDate,
        val income: Double,
        val expense: Double
    )
}
