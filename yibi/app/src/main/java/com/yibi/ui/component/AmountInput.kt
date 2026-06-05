package com.yibi.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

/**
 * 金额输入组件
 * 限制只能输入数字和小数点，最多两位小数
 */
@Composable
fun AmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "金额",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // 只允许数字和最多一个小数点，最多两位小数
            val filtered = newValue.filter { it.isDigit() || it == '.' }
            val parts = filtered.split('.')
            val validated = when {
                parts.size > 2 -> parts[0] + "." + parts[1]
                parts.size == 2 && parts[1].length > 2 -> parts[0] + "." + parts[1].take(2)
                else -> filtered
            }
            onValueChange(validated)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}
