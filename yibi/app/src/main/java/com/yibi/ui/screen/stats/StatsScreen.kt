package com.yibi.ui.screen.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yibi.ui.component.IconMapper
import com.yibi.ui.component.YibiBottomBar
import com.yibi.ui.theme.ChartColors
import com.yibi.ui.theme.ExpenseColor
import com.yibi.ui.theme.IncomeColor
import com.yibi.ui.theme.PrimaryGreen
import com.yibi.ui.viewmodel.StatsViewModel
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onNavigate: (String) -> Unit
) {
    val timeRange by viewModel.timeRange.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val expenseByCategory by viewModel.expenseByCategory.collectAsState()
    val trendData by viewModel.trendData.collectAsState()
    val expenseRanking by viewModel.expenseRanking.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计概览") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            YibiBottomBar(currentRoute = "stats", onNavigate = onNavigate)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            TabRow(selectedTabIndex = timeRange.ordinal) {
                StatsViewModel.TimeRange.values().forEach { range ->
                    Tab(
                        selected = timeRange == range,
                        onClick = { viewModel.setTimeRange(range) },
                        text = {
                            Text(
                                when (range) {
                                    StatsViewModel.TimeRange.DAY -> "日"
                                    StatsViewModel.TimeRange.WEEK -> "周"
                                    StatsViewModel.TimeRange.MONTH -> "月"
                                }
                            )
                        }
                    )
                }
            }

            SummaryCard(totalIncome, totalExpense)

            if (expenseByCategory.isNotEmpty()) {
                CategoryPieChart(expenseByCategory)
            }

            if (trendData.isNotEmpty()) {
                when (timeRange) {
                    StatsViewModel.TimeRange.DAY -> DayTrendChart(trendData)
                    StatsViewModel.TimeRange.WEEK -> WeekTrendChart(
                        data = trendData,
                        onPrevious = { viewModel.goToPreviousPeriod() },
                        onNext = { viewModel.goToNextPeriod() }
                    )
                    StatsViewModel.TimeRange.MONTH -> MonthTrendChart(
                        data = trendData,
                        onPrevious = { viewModel.goToPreviousPeriod() },
                        onNext = { viewModel.goToNextPeriod() }
                    )
                }
            }

            if (expenseRanking.isNotEmpty()) {
                RankingList(expenseRanking)
            }
        }
    }
}

@Composable
private fun SummaryCard(income: Double, expense: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AmountStat("总收入", income, IncomeColor)
            AmountStat("总支出", expense, ExpenseColor)
            AmountStat("结余", income - expense, PrimaryGreen)
        }
    }
}

@Composable
private fun AmountStat(label: String, amount: Double, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = "¥${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CategoryPieChart(data: List<StatsViewModel.CategoryStat>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "支出分类占比",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            val total = data.sumOf { it.amount }
            data.forEachIndexed { index, stat ->
                val percentage = if (total > 0) (stat.amount / total * 100) else 0.0
                val color = ChartColors[index % ChartColors.size]

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Canvas(
                        modifier = Modifier.size(12.dp)
                    ) {
                        drawCircle(color = color)
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    Text(
                        text = stat.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "¥${String.format("%.2f", stat.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    Text(
                        text = "${String.format("%.1f", percentage)}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DayTrendChart(data: List<StatsViewModel.DailyStat>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "今日收支",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            data.forEach { stat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DayStatItem("收入", stat.income, IncomeColor)
                    DayStatItem("支出", stat.expense, ExpenseColor)
                    DayStatItem("结余", stat.income - stat.expense, PrimaryGreen)
                }
            }
        }
    }
}

@Composable
private fun DayStatItem(label: String, amount: Double, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "¥${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WeekTrendChart(
    data: List<StatsViewModel.DailyStat>,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    var selectedDayStat by remember { mutableStateOf<StatsViewModel.DailyStat?>(null) }
    val dateDetailFormatter = DateTimeFormatter.ofPattern("M月d日 E")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本周收支趋势",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onPrevious) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "上一周"
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "下一周"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LineChart(
                data = data,
                dateFormatter = DateTimeFormatter.ofPattern("EEE"),
                onPrevious = onPrevious,
                onNext = onNext,
                onDataPointClick = { selectedDayStat = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }

    selectedDayStat?.let { stat ->
        AlertDialog(
            onDismissRequest = { selectedDayStat = null },
            title = { Text(stat.date.format(dateDetailFormatter)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("收入", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "+¥${String.format("%.2f", stat.income)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = IncomeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("支出", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "-¥${String.format("%.2f", stat.expense)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpenseColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("结余", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "¥${String.format("%.2f", stat.income - stat.expense)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDayStat = null }) {
                    Text("关闭")
                }
            }
        )
    }
}

@Composable
private fun MonthTrendChart(
    data: List<StatsViewModel.DailyStat>,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    var selectedDayStat by remember { mutableStateOf<StatsViewModel.DailyStat?>(null) }
    val dateDetailFormatter = DateTimeFormatter.ofPattern("M月d日 E")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本月收支趋势",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onPrevious) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "上一月"
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "下一月"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LineChart(
                data = data,
                dateFormatter = DateTimeFormatter.ofPattern("dd"),
                onPrevious = onPrevious,
                onNext = onNext,
                onDataPointClick = { selectedDayStat = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }

    selectedDayStat?.let { stat ->
        AlertDialog(
            onDismissRequest = { selectedDayStat = null },
            title = { Text(stat.date.format(dateDetailFormatter)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("收入", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "+¥${String.format("%.2f", stat.income)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = IncomeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("支出", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "-¥${String.format("%.2f", stat.expense)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpenseColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("结余", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "¥${String.format("%.2f", stat.income - stat.expense)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDayStat = null }) {
                    Text("关闭")
                }
            }
        )
    }
}

@Composable
private fun LineChart(
    data: List<StatsViewModel.DailyStat>,
    dateFormatter: DateTimeFormatter,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDataPointClick: (StatsViewModel.DailyStat) -> Unit,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = (data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0).coerceAtLeast(1.0)
    val incomeColor = IncomeColor
    val expenseColor = ExpenseColor
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val density = LocalDensity.current

    val points = data.size
    val chartLeftPx = with(density) { 40.dp.toPx() }
    val chartRightPaddingPx = with(density) { 16.dp.toPx() }

    Box(
        modifier = modifier
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    if (canvasSize.width == 0) return@detectTapGestures
                    val chartWidth = canvasSize.width - chartRightPaddingPx - chartLeftPx
                    val stepX = if (points == 1) 0f else chartWidth / (points - 1)

                    val index = if (points == 1) 0
                        else ((tapOffset.x - chartLeftPx + stepX / 2) / stepX).toInt()
                            .coerceIn(0, points - 1)

                    val dataPointX = chartLeftPx + stepX * index
                    val tapThreshold = with(density) { 40.dp.toPx() }
                    if (abs(tapOffset.x - dataPointX) < tapThreshold) {
                        onDataPointClick(data[index])
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (abs(dragAmount) > 50f) {
                        if (dragAmount < 0) {
                            onNext()
                        } else {
                            onPrevious()
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val chartLeft = chartLeftPx
            val chartRight = size.width - chartRightPaddingPx
            val chartTop = 8.dp.toPx()
            val chartBottom = size.height - 24.dp.toPx()
            val chartWidth = chartRight - chartLeft
            val chartHeight = chartBottom - chartTop

            val gridCount = 4
            for (i in 0..gridCount) {
                val y = chartTop + chartHeight * i / gridCount
                drawLine(gridColor, Offset(chartLeft, y), Offset(chartRight, y), strokeWidth = 1f)
                val labelValue = String.format("%.0f", maxValue * (gridCount - i) / gridCount)
                drawContext.canvas.nativeCanvas.drawText(
                    labelValue,
                    4.dp.toPx(),
                    y + 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor.hashCode()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )
            }

            if (points < 2) {
                val x = chartLeft + chartWidth / 2
                val incomeY = chartTop + chartHeight * (1 - (data[0].income / maxValue).toFloat().coerceIn(0f, 1f))
                val expenseY = chartTop + chartHeight * (1 - (data[0].expense / maxValue).toFloat().coerceIn(0f, 1f))

                if (data[0].income > 0) {
                    drawCircle(incomeColor, 6.dp.toPx(), Offset(x, incomeY))
                }
                if (data[0].expense > 0) {
                    drawCircle(expenseColor, 6.dp.toPx(), Offset(x, expenseY))
                }

                drawContext.canvas.nativeCanvas.drawText(
                    dateFormatter.format(data[0].date),
                    x,
                    chartBottom + 16.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = textColor.hashCode()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
                return@Canvas
            }

            val stepX = chartWidth / (points - 1)

            val incomePoints = mutableListOf<Offset>()
            val expensePoints = mutableListOf<Offset>()

            data.forEachIndexed { index, stat ->
                val x = chartLeft + stepX * index
                val incomeY = chartTop + chartHeight * (1 - (stat.income / maxValue).toFloat().coerceIn(0f, 1f))
                val expenseY = chartTop + chartHeight * (1 - (stat.expense / maxValue).toFloat().coerceIn(0f, 1f))

                if (stat.income > 0) {
                    incomePoints.add(Offset(x, incomeY))
                    drawCircle(incomeColor, 6.dp.toPx(), Offset(x, incomeY))
                }
                if (stat.expense > 0) {
                    expensePoints.add(Offset(x, expenseY))
                    drawCircle(expenseColor, 6.dp.toPx(), Offset(x, expenseY))
                }

                if (index % maxOf(1, (points - 1) / 7) == 0 || index == points - 1) {
                    drawContext.canvas.nativeCanvas.drawText(
                        dateFormatter.format(stat.date),
                        x,
                        chartBottom + 16.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = textColor.hashCode()
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            if (incomePoints.size >= 2) {
                val incomePath = Path().apply {
                    moveTo(incomePoints[0].x, incomePoints[0].y)
                    for (i in 1 until incomePoints.size) {
                        lineTo(incomePoints[i].x, incomePoints[i].y)
                    }
                }
                drawPath(
                    path = incomePath,
                    color = incomeColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            if (expensePoints.size >= 2) {
                val expensePath = Path().apply {
                    moveTo(expensePoints[0].x, expensePoints[0].y)
                    for (i in 1 until expensePoints.size) {
                        lineTo(expensePoints[i].x, expensePoints[i].y)
                    }
                }
                drawPath(
                    path = expensePath,
                    color = expenseColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            drawLine(
                color = incomeColor,
                start = Offset(16.dp.toPx(), chartBottom + 28.dp.toPx()),
                end = Offset(64.dp.toPx(), chartBottom + 28.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawContext.canvas.nativeCanvas.drawText(
                "收入",
                68.dp.toPx(),
                chartBottom + 32.dp.toPx(),
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = 10.sp.toPx()
                }
            )
            drawLine(
                color = expenseColor,
                start = Offset(100.dp.toPx(), chartBottom + 28.dp.toPx()),
                end = Offset(148.dp.toPx(), chartBottom + 28.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawContext.canvas.nativeCanvas.drawText(
                "支出",
                152.dp.toPx(),
                chartBottom + 32.dp.toPx(),
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = 10.sp.toPx()
                }
            )
        }
    }
}

@Composable
private fun RankingList(data: List<StatsViewModel.CategoryStat>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "支出排行",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            data.forEachIndexed { index, stat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (index < 3) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp)
                    )

                    Icon(
                        imageVector = IconMapper.getIcon(stat.icon),
                        contentDescription = stat.categoryName,
                        tint = PrimaryGreen,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Text(
                        text = stat.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "¥${String.format("%.2f", stat.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
