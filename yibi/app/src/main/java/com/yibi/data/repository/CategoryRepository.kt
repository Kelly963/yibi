package com.yibi.data.repository

import com.yibi.data.local.CategoryDao
import com.yibi.data.local.DatabaseCallback
import com.yibi.data.local.entity.CategoryEntity
import com.yibi.domain.expandable.CategoryManagement
import com.yibi.domain.model.Category
import com.yibi.domain.model.ModuleContext
import com.yibi.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * 分类Repository实现
 * 实现CategoryManagement接口，提供分类的增删改查
 */
class CategoryRepository(
    private val categoryDao: CategoryDao
) : CategoryManagement {

    override val moduleId: String = "category_management"
    override val displayName: String = "分类管理"

    override fun onInitialize(context: ModuleContext) {
        // Repository已通过构造函数注入Dao
    }

    override fun onDestroy() {
        // 无需释放资源
    }

    // ==================== CategoryManagement 接口实现 ====================

    override fun addCategory(name: String, type: TransactionType, icon: String): String {
        require(name.isNotBlank()) { "分类名称不能为空" }
        return UUID.randomUUID().toString()
    }

    /**
     * 异步添加分类（实际持久化）
     */
    suspend fun addCategoryAsync(
        name: String,
        type: TransactionType,
        icon: String,
        sortOrder: Int = 0
    ): String = withContext(Dispatchers.IO) {
        require(name.isNotBlank()) { "分类名称不能为空" }

        val categoryId = UUID.randomUUID().toString()
        val entity = CategoryEntity(
            id = categoryId,
            name = name,
            type = type.name,
            icon = icon,
            sortOrder = sortOrder,
            isBuiltIn = false
        )
        categoryDao.insert(entity)
        categoryId
    }

    override fun updateCategory(categoryId: String, name: String, icon: String) {
        require(categoryId.isNotBlank()) { "分类ID不能为空" }
        require(name.isNotBlank()) { "分类名称不能为空" }
    }

    /**
     * 异步更新分类
     */
    suspend fun updateCategoryAsync(categoryId: String, name: String, icon: String) = withContext(Dispatchers.IO) {
        require(categoryId.isNotBlank()) { "分类ID不能为空" }
        require(name.isNotBlank()) { "分类名称不能为空" }

        val existing = categoryDao.getById(categoryId)
            ?: throw IllegalArgumentException("分类不存在: $categoryId")

        val updated = existing.copy(name = name, icon = icon)
        categoryDao.update(updated)
    }

    override fun deleteCategory(categoryId: String) {
        require(categoryId.isNotBlank()) { "分类ID不能为空" }
    }

    /**
     * 异步删除分类（软删除：隐藏而非真删）
     */
    suspend fun deleteCategoryAsync(categoryId: String) = withContext(Dispatchers.IO) {
        require(categoryId.isNotBlank()) { "分类ID不能为空" }

        val existing = categoryDao.getById(categoryId)
            ?: throw IllegalArgumentException("分类不存在: $categoryId")

        if (existing.isBuiltIn) {
            // 内置分类只能隐藏，不能彻底删除
            categoryDao.hideCategory(categoryId)
        } else {
            // 用户自定义分类也采用隐藏方式
            categoryDao.hideCategory(categoryId)
        }
    }

    /**
     * 恢复（取消隐藏）分类
     */
    suspend fun unhideCategoryAsync(categoryId: String) = withContext(Dispatchers.IO) {
        require(categoryId.isNotBlank()) { "分类ID不能为空" }
        categoryDao.unhideCategory(categoryId)
    }

    override fun getAllCategories(): List<Category> {
        return emptyList()
    }

    /**
     * 获取所有分类Flow（包含隐藏的，用于管理页面）
     */
    fun getAllCategoriesFlow(): Flow<List<Category>> {
        return categoryDao.getAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * 获取可见分类Flow（不包含隐藏的，用于记账页面）
     */
    fun getVisibleCategoriesFlow(): Flow<List<Category>> {
        return categoryDao.getVisible().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * 异步获取所有分类列表
     */
    suspend fun getAllCategoriesList(): List<Category> = withContext(Dispatchers.IO) {
        categoryDao.getAllList().map { it.toDomainModel() }
    }

    override fun getCategoriesByType(type: TransactionType): List<Category> {
        return emptyList()
    }

    /**
     * 异步按类型获取分类
     */
    suspend fun getCategoriesByTypeAsync(type: TransactionType): List<Category> = withContext(Dispatchers.IO) {
        categoryDao.getByType(type.name).map { it.toDomainModel() }
    }

    /**
     * 按类型获取分类Flow
     */
    fun getCategoriesByTypeFlow(type: TransactionType): Flow<List<Category>> {
        return categoryDao.getByTypeFlow(type.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCategory(categoryId: String): Category? {
        return null
    }

    /**
     * 异步获取单个分类
     */
    suspend fun getCategoryAsync(categoryId: String): Category? = withContext(Dispatchers.IO) {
        categoryDao.getById(categoryId)?.toDomainModel()
    }

    // ==================== 初始化 ====================

    /**
     * 初始化预置分类数据（应用首次启动时调用）
     */
    suspend fun initializeBuiltInCategories() = withContext(Dispatchers.IO) {
        val count = categoryDao.getBuiltInCount()
        if (count == 0) {
            DatabaseCallback.getBuiltInCategories().forEach {
                categoryDao.insert(it)
            }
        }
    }

    // ==================== 实体转换 ====================

    private fun CategoryEntity.toDomainModel(): Category {
        return Category(
            id = id,
            name = name,
            type = TransactionType.valueOf(type),
            icon = icon,
            sortOrder = sortOrder,
            isBuiltIn = isBuiltIn,
            isHidden = isHidden
        )
    }
}
