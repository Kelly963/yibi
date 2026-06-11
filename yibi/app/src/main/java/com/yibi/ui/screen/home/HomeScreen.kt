package com.yibi.ui.screen.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.yibi.domain.model.Transaction
import com.yibi.domain.model.TransactionType
import com.yibi.ui.component.TransactionItem
import com.yibi.ui.component.YibiBottomBar
import com.yibi.ui.theme.ExpenseColor
import com.yibi.ui.theme.IncomeColor
import com.yibi.ui.theme.PrimaryGreen
import com.yibi.ui.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Transaction) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val todayIncome by viewModel.todayIncome.collectAsState()
    val todayExpense by viewModel.todayExpense.collectAsState()
    val monthIncome by viewModel.monthIncome.collectAsState()
    val monthExpense by viewModel.monthExpense.collectAsState()
    val fabOffset by viewModel.fabOffset.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("一笔记账") },
                    actions = {
                        IconButton(onClick = { onNavigate("about") }) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "关于",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryGreen,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                YibiBottomBar(currentRoute = "home", onNavigate = onNavigate)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                OverviewCard(
                    todayIncome = todayIncome,
                    todayExpense = todayExpense,
                    monthIncome = monthIncome,
                    monthExpense = monthExpense
                )

                Text(
                    text = "近期记录",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                val groupedTransactions = remember(transactions) {
                    transactions.groupBy { it.date }.toList()
                        .sortedByDescending { it.first }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    groupedTransactions.forEach { (date, dayTransactions) ->
                        val dayIncome = dayTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                        val dayExpense = dayTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                        item(key = "header_$date") {
                            DateHeader(
                                date = date,
                                dayIncome = dayIncome,
                                dayExpense = dayExpense
                            )
                        }

                        items(
                            items = dayTransactions,
                            key = { it.id }
                        ) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                categoryName = viewModel.getCategoryName(transaction.categoryId),
                                categoryIcon = viewModel.getCategoryIcon(transaction.categoryId),
                                onClick = { onEditTransaction(transaction) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddTransaction,
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
            Icon(Icons.Default.Add, contentDescription = "记一笔")
        }
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    dayIncome: Double,
    dayExpense: Double
) {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    val dateText = when (date) {
        today -> "今天"
        yesterday -> "昨天"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("M月d日 E", Locale.CHINESE)
            date.format(formatter)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (dayIncome > 0) {
                Text(
                    text = "+¥${String.format("%.2f", dayIncome)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = IncomeColor
                )
            }
            if (dayIncome > 0 && dayExpense > 0) {
                Text(
                    text = " / ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (dayExpense > 0) {
                Text(
                    text = "-¥${String.format("%.2f", dayExpense)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ExpenseColor
                )
            }
        }
    }
}

@Composable
private fun OverviewCard(
    todayIncome: Double,
    todayExpense: Double,
    monthIncome: Double,
    monthExpense: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "本月概览",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AmountColumn("本月收入", monthIncome, IncomeColor)
                AmountColumn("本月支出", monthExpense, ExpenseColor)
                AmountColumn("本月结余", monthIncome - monthExpense, PrimaryGreen)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AmountColumn("今日收入", todayIncome, IncomeColor, isSmall = true)
                AmountColumn("今日支出", todayExpense, ExpenseColor, isSmall = true)
            }
        }
    }
}

@Composable
private fun AmountColumn(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    isSmall: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = if (isSmall) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = "¥${String.format("%.2f", amount)}",
            style = if (isSmall) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
