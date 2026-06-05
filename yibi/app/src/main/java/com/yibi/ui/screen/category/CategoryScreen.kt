package com.yibi.ui.screen.category

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.yibi.domain.model.Category
import com.yibi.domain.model.TransactionType
import com.yibi.ui.component.IconMapper
import com.yibi.ui.component.YibiBottomBar
import com.yibi.ui.theme.ExpenseColor
import com.yibi.ui.theme.IncomeColor
import com.yibi.ui.theme.PrimaryGreen
import com.yibi.ui.viewmodel.CategoryViewModel

/**
 * 分类管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel,
    onNavigate: (String) -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val fabOffset by viewModel.fabOffset.collectAsState()

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var hidingCategory by remember { mutableStateOf<Category?>(null) }

    // 操作状态提示
    when (operationState) {
        is CategoryViewModel.OperationState.Success -> {
            viewModel.resetState()
        }
        else -> {}
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("分类管理") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryGreen,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                YibiBottomBar(currentRoute = "category", onNavigate = onNavigate)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TabRow(selectedTabIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1) {
                    Tab(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        text = {
                            Text(
                                "支出",
                                color = if (selectedType == TransactionType.EXPENSE) ExpenseColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                    Tab(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        text = {
                            Text(
                                "收入",
                                color = if (selectedType == TransactionType.INCOME) IncomeColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }

                val visibleCategories = categories.filter { it.type == selectedType && !it.isHidden }
                val hiddenCategories = categories.filter { it.type == selectedType && it.isHidden }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(visibleCategories) { category ->
                        CategoryListItem(
                            category = category,
                            isHidden = false,
                            onEdit = { editingCategory = category },
                            onHide = { hidingCategory = category }
                        )
                    }

                    if (hiddenCategories.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "已隐藏（不显示在记账中）",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        items(hiddenCategories) { category ->
                            CategoryListItem(
                                category = category,
                                isHidden = true,
                                onEdit = null,
                                onHide = null,
                                onUnhide = { viewModel.unhideCategory(category.id) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PrimaryGreen,
            modifier = Modifier
                .offset { IntOffset(fabOffset.first.roundToInt(), fabOffset.second.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        viewModel.updateFabOffset(
                            fabOffset.first + dragAmount.x,
                            fabOffset.second + dragAmount.y
                        )
                    }
                }
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加分类")
        }
    }

    // 添加/编辑对话框
    if (showAddDialog || editingCategory != null) {
        CategoryDialog(
            category = editingCategory,
            type = selectedType,
            onDismiss = {
                showAddDialog = false
                editingCategory = null
            },
            onConfirm = { name, icon ->
                if (editingCategory != null) {
                    viewModel.updateCategory(editingCategory!!.id, name, icon)
                } else {
                    viewModel.addCategory(name, selectedType, icon)
                }
                showAddDialog = false
                editingCategory = null
            }
        )
    }

    // 隐藏确认对话框
    if (hidingCategory != null) {
        AlertDialog(
            onDismissRequest = { hidingCategory = null },
            title = { Text("隐藏分类") },
            text = { Text("隐藏后「${hidingCategory!!.name}」将不会出现在记账的分类选择中，但可以随时恢复。确定要隐藏吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.hideCategory(hidingCategory!!.id)
                        hidingCategory = null
                    }
                ) {
                    Text("隐藏", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { hidingCategory = null }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 分类列表项
 */
@Composable
private fun CategoryListItem(
    category: Category,
    isHidden: Boolean,
    onEdit: (() -> Unit)? = null,
    onHide: (() -> Unit)? = null,
    onUnhide: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = if (isHidden) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) else CardDefaults.cardColors()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(
                imageVector = IconMapper.getIcon(category.icon),
                contentDescription = category.name,
                tint = if (isHidden) MaterialTheme.colorScheme.onSurfaceVariant else PrimaryGreen,
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = category.name + if (isHidden) " (已隐藏)" else "",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isHidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // 已隐藏：显示恢复按钮
            if (isHidden) {
                TextButton(onClick = { onUnhide?.invoke() }) {
                    Text("恢复", color = PrimaryGreen)
                }
            } else {
                // 可见分类：编辑 + 隐藏按钮
                IconButton(onClick = { onEdit?.invoke() }) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }
                IconButton(onClick = { onHide?.invoke() }) {
                    Icon(
                        Icons.Default.VisibilityOff,
                        contentDescription = "隐藏",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 分类编辑/添加对话框
 */
@Composable
private fun CategoryDialog(
    category: Category?,
    type: TransactionType,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var icon by remember { mutableStateOf(category?.icon ?: "more_horiz") }

    val iconOptions = listOf(
        "restaurant" to "餐饮",
        "directions_car" to "交通",
        "shopping_cart" to "购物",
        "sports_esports" to "娱乐",
        "home" to "居住",
        "local_hospital" to "医疗",
        "school" to "教育",
        "phone_android" to "通讯",
        "checkroom" to "服饰",
        "face" to "美妆",
        "pets" to "宠物",
        "flight" to "旅行",
        "fitness_center" to "运动",
        "local_cafe" to "零食饮料",
        "volunteer_activism" to "人情往来",
        "account_balance_wallet" to "钱包/工资",
        "card_giftcard" to "礼物/奖金",
        "trending_up" to "投资",
        "work" to "工作/兼职",
        "redeem" to "红包/退款",
        "more_horiz" to "其他"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category != null) "编辑分类" else "添加分类") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("选择图标", style = MaterialTheme.typography.labelLarge)

                Spacer(modifier = Modifier.height(8.dp))

                // 图标分两行显示
                iconOptions.chunked(6).forEach { rowIcons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowIcons.forEach { (iconName, _) ->
                            IconButton(
                                onClick = { icon = iconName }
                            ) {
                                Icon(
                                    imageVector = IconMapper.getIcon(iconName),
                                    contentDescription = iconName,
                                    tint = if (icon == iconName) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, icon)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
