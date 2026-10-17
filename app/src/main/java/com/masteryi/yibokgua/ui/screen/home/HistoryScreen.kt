package com.masteryi.yibokgua.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masteryi.yibokgua.ui.components.AdaptiveScreen
import com.masteryi.yibokgua.ui.components.ZenCard
import com.masteryi.yibokgua.ui.components.zenThinBorder
import com.masteryi.yibokgua.ui.export.shareDetailCardAsImage
import com.masteryi.yibokgua.viewmodel.DivinationViewModel
import java.text.SimpleDateFormat
import java.util.*

private val ThinBorder: BorderStroke
    @Composable get() = zenThinBorder()

@Composable
fun HistoryScreen(
    viewModel: DivinationViewModel,
    onNavigateToDetail: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val results = uiState.historyList
    val context = LocalContext.current

    AdaptiveScreen {
        if (results.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("尚無起卦記錄", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            items(results, key = { it.timestamp }) { result ->
                val analysis = remember(result) { viewModel.analyzeResult(result) }
                val timeStr = remember(result.timestamp) {
                    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                        .format(Date(result.timestamp))
                }
                ZenCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToDetail(result.originalHexagram.id) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    result.divinationType,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                result.question.takeIf { it.isNotBlank() }?.let { question ->
                                    Text(
                                        "所占之事：$question",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                result.divinationText.takeIf { it.isNotBlank() }?.let { text ->
                                    Text(
                                        "占字：$text",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(timeStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "${result.originalHexagram.symbol} ${result.originalHexagram.name}",
                                fontSize = 22.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    shareDetailCardAsImage(context, result, analysis)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("導出詳情卡片")
                            }
                        }
                    }
                }
            }
        }
    }
    }
}
