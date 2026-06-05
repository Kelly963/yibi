package com.yibi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yibi.data.repository.CategoryRepository
import com.yibi.domain.model.Category
import com.yibi.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 分类管理ViewModel
 */
class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    // 所有分类
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    // 操作状态
    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState

    // FAB 可拖动位置
    private val _fabOffset = MutableStateFlow(Pair(0f, 0f))
    val fabOffset: StateFlow<Pair<Float, Float>> = _fabOffset

    fun updateFabOffset(x: Float, y: Float) {
        _fabOffset.value = Pair(x, y)
    }

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategoriesFlow().collect { list ->
                _categories.value = list
            }
        }
    }

    fun addCategory(name: String, type: TransactionType, icon: String) {
        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading
                categoryRepository.addCategoryAsync(name, type, icon)
                _operationState.value = OperationState.Success("添加成功")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "添加失败")
            }
        }
    }

    fun updateCategory(categoryId: String, name: String, icon: String) {
        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading
                categoryRepository.updateCategoryAsync(categoryId, name, icon)
                _operationState.value = OperationState.Success("更新成功")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "更新失败")
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading
                categoryRepository.deleteCategoryAsync(categoryId)
                _operationState.value = OperationState.Success("已隐藏")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "操作失败")
            }
        }
    }

    fun hideCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading
                categoryRepository.deleteCategoryAsync(categoryId)
                _operationState.value = OperationState.Success("已隐藏")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "隐藏失败")
            }
        }
    }

    fun unhideCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                _operationState.value = OperationState.Loading
                categoryRepository.unhideCategoryAsync(categoryId)
                _operationState.value = OperationState.Success("已恢复显示")
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "恢复失败")
            }
        }
    }

    fun resetState() {
        _operationState.value = OperationState.Idle
    }

    sealed class OperationState {
        object Idle : OperationState()
        object Loading : OperationState()
        data class Success(val message: String) : OperationState()
        data class Error(val message: String) : OperationState()
    }
}
