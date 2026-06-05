package com.yibi.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yibi.domain.model.Category
import com.yibi.ui.theme.PrimaryGreen

/**
 * 分类选择芯片组件
 */
@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = IconMapper.getIcon(category.icon),
                contentDescription = category.name,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(12.dp)
                    .size(24.dp)
            )
        }
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
