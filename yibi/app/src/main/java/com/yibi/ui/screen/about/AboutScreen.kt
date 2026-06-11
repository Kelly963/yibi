package com.yibi.ui.screen.about

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yibi.ui.theme.PrimaryGreen
import com.yibi.ui.viewmodel.AboutViewModel
import com.yibi.util.UpdateChecker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: AboutViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val releases by viewModel.releases.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentVersion by viewModel.currentVersion.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于应用") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "当前版本",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "v${currentVersion}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.checkUpdate(context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("检查更新")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "版本日志",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (releases.isEmpty()) {
                Text(
                    text = "暂无版本记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                releases.forEachIndexed { index, release ->
                    ReleaseItem(release, context)
                    if (index < releases.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReleaseItem(
    release: UpdateChecker.ReleaseInfo,
    context: android.content.Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = release.tagName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = release.publishedAt.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (release.htmlUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "查看详情",
                        modifier = Modifier.clickable {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl)))
                            } catch (e: Exception) {
                                Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
                            }
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (release.body.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = release.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
