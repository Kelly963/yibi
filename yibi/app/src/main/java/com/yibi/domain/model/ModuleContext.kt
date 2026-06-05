package com.yibi.domain.model

import com.yibi.data.local.AppDatabase

/**
 * 模块上下文，提供模块运行所需的公共资源
 */
data class ModuleContext(
    val database: AppDatabase,
    val resourceProvider: ResourceProvider
)

/**
 * 资源提供者接口
 */
interface ResourceProvider {
    fun getString(resId: Int): String
    fun getString(resId: Int, vararg formatArgs: Any): String
}
