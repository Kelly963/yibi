package com.yibi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibi.data.repository.CategoryRepository
import com.yibi.data.repository.TransactionRepository
import com.yibi.domain.model.Category
import com.yibi.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 记账页面ViewModel
 */
class RecordViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // 分类列表
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    // 当前交易类型
    private val _transactionType = MutableStateFlow(TransactionType.EXPENSE)
    val transactionType: StateFlow<TransactionType> = _transactionType

    // 选中的分类
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    // 金额
    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount

    // 日期
    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date

    // 备注
    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note

    // 保存状态
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    // 编辑模式
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode

    private var editTransactionId: String? = null

    // 删除回调（删除成功后通知UI返回）
    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            // 使用可见分类（排除隐藏的分类）
            categoryRepository.getVisibleCategoriesFlow().collect { list ->
                _categories.value = list
                // 默认选中第一个对应类型的分类
                if (_selectedCategory.value == null) {
                    _selectedCategory.value = list.firstOrNull { it.type == _transactionType.value }
                }
            }
        }
    }

    fun setTransactionType(type: TransactionType) {
        _transactionType.value = type
        // 切换类型时，选中该类型的第一个分类
        val typeCategories = _categories.value.filter { it.type == type }
        _selectedCategory.value = typeCategories.firstOrNull()
    }

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
    }

    fun setAmount(value: String) {
        _amount.value = value
    }

    fun setDate(newDate: LocalDate) {
        _date.value = newDate
    }

    fun setNote(value: String) {
        _note.value = value
    }

    fun prepareEdit(
        transactionId: String,
        type: TransactionType,
        amount: Double,
        categoryId: String,
        date: LocalDate,
        note: String
    ) {
        editTransactionId = transactionId
        _isEditMode.value = true
        _transactionType.value = type
        _amount.value = amount.toString()
        _date.value = date
        _note.value = note
        _selectedCategory.value = _categories.value.find { it.id == categoryId }
    }

    fun reset() {
        editTransactionId = null
        _isEditMode.value = false
        _transactionType.value = TransactionType.EXPENSE
        _amount.value = ""
        _date.value = LocalDate.now()
        _note.value = ""
        _saveState.value = SaveState.Idle
        _deleteState.value = DeleteState.Idle
        val expenseCategories = _categories.value.filter { it.type == TransactionType.EXPENSE }
        _selectedCategory.value = expenseCategories.firstOrNull()
    }

    /**
     * 删除当前编辑的交易记录
     */
    fun deleteCurrentTransaction() {
        viewModelScope.launch {
            try {
                _deleteState.value = DeleteState.Deleting
                val id = editTransactionId
                if (id != null) {
                    transactionRepository.deleteTransactionAsync(id)
                    _deleteState.value = DeleteState.Success
                } else {
                    _deleteState.value = DeleteState.Error("没有可删除的记录")
                }
            } catch (e: Exception) {
                _deleteState.value = DeleteState.Error(e.message ?: "删除失败")
            }
        }
    }

    fun saveTransaction() {
        viewModelScope.launch {
            try {
                _saveState.value = SaveState.Saving

                val amountValue = _amount.value.toDoubleOrNull()
                    ?: throw IllegalArgumentException("请输入有效金额")
                if (amountValue <= 0) throw IllegalArgumentException("金额必须大于0")

                val category = _selectedCategory.value
                    ?: throw IllegalArgumentException("请选择分类")

                if (_isEditMode.value && editTransactionId != null) {
                    // 更新
                    transactionRepository.updateTransactionAsync(
                        transactionId = editTransactionId!!,
                        type = _transactionType.value,
                        amount = amountValue,
                        categoryId = category.id,
                        date = _date.value,
                        note = _note.value
                    )
                } else {
                    // 新增
                    transactionRepository.insertTransaction(
                        type = _transactionType.value,
                        amount = amountValue,
                        categoryId = category.id,
                        date = _date.value,
                        note = _note.value
                    )
                }

                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "保存失败")
            }
        }
    }

    sealed class SaveState {
        object Idle : SaveState()
        object Saving : SaveState()
        object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }

    sealed class DeleteState {
        object Idle : DeleteState()
        object Deleting : DeleteState()
        object Success : DeleteState()
        data class Error(val message: String) : DeleteState()
    }
}
