package com.yibi.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yibi.domain.model.Transaction
import com.yibi.domain.model.TransactionType
import com.yibi.ui.theme.ExpenseColor
import com.yibi.ui.theme.IncomeColor
import java.time.format.DateTimeFormatter

/**
 * 交易记录列表项组件
 */
@Composable
fun TransactionItem(
    transaction: Transaction,
    categoryName: String,
    categoryIcon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd")
    val amountColor = if (transaction.type == TransactionType.INCOME) IncomeColor else ExpenseColor
    val amountPrefix = if (transaction.type == TransactionType.INCOME) "+" else "-"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 分类图标
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = IconMapper.getIcon(categoryIcon),
                    contentDescription = categoryName,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 分类名称和日期
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaction.date.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // 金额
            Text(
                text = "$amountPrefix¥${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
