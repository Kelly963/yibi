package com.yibi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yibi.data.local.AppDatabase
import com.yibi.data.repository.CategoryRepository
import com.yibi.data.repository.TransactionRepository
import com.yibi.domain.model.Transaction
import com.yibi.ui.screen.about.AboutScreen
import com.yibi.ui.screen.category.CategoryScreen
import com.yibi.ui.screen.home.HomeScreen
import com.yibi.ui.screen.record.RecordScreen
import com.yibi.ui.screen.stats.StatsScreen
import com.yibi.ui.theme.YibiTheme
import com.yibi.ui.viewmodel.AboutViewModel
import com.yibi.ui.viewmodel.CategoryViewModel
import com.yibi.ui.viewmodel.HomeViewModel
import com.yibi.ui.viewmodel.RecordViewModel
import com.yibi.ui.viewmodel.StatsViewModel
import com.yibi.util.UpdateChecker

/**
 * 主Activity - 应用入口
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 手动依赖注入（按需求报告要求）
        val database = AppDatabase.getDatabase(applicationContext)
        val transactionDao = database.transactionDao()
        val categoryDao = database.categoryDao()

        val transactionRepository = TransactionRepository(transactionDao)
        val categoryRepository = CategoryRepository(categoryDao)

        val homeViewModel = HomeViewModel(transactionRepository, categoryRepository)
        val recordViewModel = RecordViewModel(transactionRepository, categoryRepository)
        val categoryViewModel = CategoryViewModel(categoryRepository)
        val statsViewModel = StatsViewModel(transactionRepository, categoryRepository)
        val aboutViewModel = AboutViewModel()

        // 启动时检查更新
        UpdateChecker.checkAndShowUpdateDialog(this)

        setContent {
            YibiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        // 首页
                        composable("home") {
                            // 标记是否从底部导航进入记录页（需要重置状态）
                            var shouldResetRecord by remember { mutableStateOf(false) }

                            HomeScreen(
                                viewModel = homeViewModel,
                                onNavigate = { route ->
                                    if (route == "record") {
                                        shouldResetRecord = true
                                    }
                                    navController.navigate(route) {
                                        popUpTo("home") { inclusive = false }
                                    }
                                },
                                onAddTransaction = {
                                    recordViewModel.reset()
                                    navController.navigate("record")
                                },
                                onEditTransaction = { transaction ->
                                    recordViewModel.prepareEdit(
                                        transactionId = transaction.id,
                                        type = transaction.type,
                                        amount = transaction.amount,
                                        categoryId = transaction.categoryId,
                                        date = transaction.date,
                                        note = transaction.note
                                    )
                                    navController.navigate("record")
                                }
                            )
                        }

                        // 统计页面
                        composable("stats") {
                            StatsScreen(
                                viewModel = statsViewModel,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo("home") { inclusive = false }
                                    }
                                }
                            )
                        }

                        // 分类管理页面
                        composable("category") {
                            CategoryScreen(
                                viewModel = categoryViewModel,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo("home") { inclusive = false }
                                    }
                                }
                            )
                        }

                        // 记账页面
                        composable("record") {
                            // 从底部导航进入时重置状态
                            LaunchedEffect(Unit) {
                                // 检查是否是编辑模式，如果不是则重置
                                // 编辑模式通过 prepareEdit 设置，此时 isEditMode 为 true
                                // 如果从底部导航进入，isEditMode 应该为 false，需要重置
                                if (!recordViewModel.isEditMode.value) {
                                    recordViewModel.reset()
                                }
                            }
                            RecordScreen(
                                viewModel = recordViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 关于页面
                        composable("about") {
                            AboutScreen(
                                viewModel = aboutViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
