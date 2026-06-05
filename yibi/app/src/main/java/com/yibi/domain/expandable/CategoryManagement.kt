package com.yibi.domain.expandable

import com.yibi.domain.model.ModuleContext
import com.yibi.domain.model.TransactionType

/**
 * 分类管理扩展接口，定义分类目录的扩展点。
 */
interface CategoryManagement : ExpandableUnit {
    override val moduleId: String get() = "category_management"
    override val displayName: String get() = "分类管理"

    override fun onInitialize(context: ModuleContext) {
        // 初始化分类数据
    }

    override fun onDestroy() {
        // 释放资源
    }

    /**
     * 添加分类，返回分类ID。
     * @param name 分类名称
     * @param type 收入或支出类型
     * @param icon 图标标识
     * @return 分类唯一标识
     */
    fun addCategory(name: String, type: TransactionType, icon: String): String

    /**
     * 更新分类。
     */
    fun updateCategory(categoryId: String, name: String, icon: String)

    /**
     * 删除分类。
     */
    fun deleteCategory(categoryId: String)

    /**
     * 获取所有分类。
     */
    fun getAllCategories(): List<com.yibi.domain.model.Category>

    /**
     * 按类型获取分类。
     */
    fun getCategoriesByType(type: TransactionType): List<com.yibi.domain.model.Category>

    /**
     * 获取单个分类。
     */
    fun getCategory(categoryId: String): com.yibi.domain.model.Category?
}
