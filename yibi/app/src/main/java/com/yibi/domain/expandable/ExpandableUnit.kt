package com.yibi.domain.expandable

import com.yibi.domain.model.ModuleContext

/**
 * 可扩展单元基接口，所有可扩展模块均实现此接口。
 * 后续扩展功能（预算提醒、多币种、账单导入）通过实现此接口接入。
 */
interface ExpandableUnit {
    /** 模块唯一标识 */
    val moduleId: String
    /** 模块显示名称 */
    val displayName: String
    /** 初始化模块资源 */
    fun onInitialize(context: ModuleContext)
    /** 销毁模块资源 */
    fun onDestroy()
}
