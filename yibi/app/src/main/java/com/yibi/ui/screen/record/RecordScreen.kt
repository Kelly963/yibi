package com.yibi.ui.screen.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yibi.domain.model.TransactionType
import com.yibi.ui.component.AmountInput
import com.yibi.ui.component.CategoryChip
import com.yibi.ui.theme.ExpenseColor
import com.yibi.ui.theme.IncomeColor
import com.yibi.ui.viewmodel.RecordViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 记账页面 - 新增/编辑交易记录
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecordScreen(
    viewModel: RecordViewModel,
    onNavigateBack: () -> Unit
) {
    val transactionType by viewModel.transactionType.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val date by viewModel.date.collectAsState()
    val note by viewModel.note.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    // 保存成功后返回
    LaunchedEffect(saveState) {
        if (saveState is RecordViewModel.SaveState.Success) {
            onNavigateBack()
        }
    }

    // 删除成功后返回
    LaunchedEffect(deleteState) {
        if (deleteState is RecordViewModel.DeleteState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "编辑记录" else "记一笔") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 编辑模式显示删除按钮
                    if (isEditMode) {
                        IconButton(
                            onClick = { viewModel.deleteCurrentTransaction() }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除记录",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 收入/支出切换
            TabRow(selectedTabIndex = if (transactionType == TransactionType.EXPENSE) 0 else 1) {
                Tab(
                    selected = transactionType == TransactionType.EXPENSE,
                    onClick = { viewModel.setTransactionType(TransactionType.EXPENSE) },
                    text = {
                        Text(
                            "支出",
                            color = if (transactionType == TransactionType.EXPENSE) ExpenseColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                Tab(
                    selected = transactionType == TransactionType.INCOME,
                    onClick = { viewModel.setTransactionType(TransactionType.INCOME) },
                    text = {
                        Text(
                            "收入",
                            color = if (transactionType == TransactionType.INCOME) IncomeColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 金额输入
            AmountInput(
                value = amount,
                onValueChange = viewModel::setAmount,
                label = "金额（元）"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 日期选择
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = date.toString(),
                    onValueChange = {},
                    label = { Text("日期") },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "选择日期")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 分类选择
            Text(
                text = "选择分类",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            val filteredCategories = categories.filter { it.type == transactionType }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                filteredCategories.forEach { category ->
                    CategoryChip(
                        category = category,
                        isSelected = selectedCategory?.id == category.id,
                        onClick = { viewModel.selectCategory(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 备注输入
            OutlinedTextField(
                value = note,
                onValueChange = viewModel::setNote,
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 保存按钮
            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier.fillMaxWidth(),
                enabled = saveState !is RecordViewModel.SaveState.Saving
            ) {
                Text(
                    text = when (saveState) {
                        is RecordViewModel.SaveState.Saving -> "保存中..."
                        else -> if (isEditMode) "更新" else "保存"
                    }
                )
            }

            // 错误提示
            if (saveState is RecordViewModel.SaveState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (saveState as RecordViewModel.SaveState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.setDate(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
