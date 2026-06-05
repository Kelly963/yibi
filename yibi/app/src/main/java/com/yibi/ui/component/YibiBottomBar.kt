package com.yibi.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yibi.R

/**
 * 底部导航栏组件
 */
@Composable
fun YibiBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem("home", stringResource(R.string.nav_home), Icons.Default.Home),
        BottomNavItem("stats", stringResource(R.string.nav_stats), Icons.Default.BarChart),
        BottomNavItem("category", stringResource(R.string.nav_category), Icons.Default.Category)
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
