package com.masteryi.yibokgua

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.masteryi.yibokgua.ui.screen.input.DivinationInputScreen
import com.masteryi.yibokgua.ui.screen.query.HexagramQueryScreen
import com.masteryi.yibokgua.ui.theme.YibokguaTheme
import com.masteryi.yibokgua.viewmodel.DivinationViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DivinationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YibokguaTheme {
                var currentTab by remember { mutableIntStateOf(0) }
                var pendingDetailId by remember { mutableStateOf<Int?>(null) }

                // 全局自適應：寬螢幕（平板 / 大視窗）使用側邊 NavigationRail，窄螢幕使用底部 NavigationBar
                val isWide = LocalConfiguration.current.screenWidthDp >= 600

                if (isWide) {
                    Scaffold {
                        Row(modifier = Modifier.fillMaxSize()) {
                            NavigationRail {
                                NavigationRailItems(
                                    currentTab = currentTab,
                                    onSelect = { currentTab = it }
                                )
                            }
                            VerticalDivider()
                            MainScreenContent(
                                currentTab = currentTab,
                                viewModel = viewModel,
                                pendingDetailId = pendingDetailId,
                                onNavigateToDetail = { hexagramId ->
                                    pendingDetailId = hexagramId
                                    currentTab = 1
                                },
                                onAutoOpenConsumed = { pendingDetailId = null },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItems(
                                    currentTab = currentTab,
                                    onSelect = { currentTab = it }
                                )
                            }
                        }
                    ) { innerPadding ->
                        MainScreenContent(
                            currentTab = currentTab,
                            viewModel = viewModel,
                            pendingDetailId = pendingDetailId,
                            onNavigateToDetail = { hexagramId ->
                                pendingDetailId = hexagramId
                                currentTab = 1
                            },
                            onAutoOpenConsumed = { pendingDetailId = null },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainScreenContent(
    currentTab: Int,
    viewModel: DivinationViewModel,
    pendingDetailId: Int?,
    onNavigateToDetail: (Int) -> Unit,
    onAutoOpenConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        when (currentTab) {
            0 -> DivinationInputScreen(
                viewModel = viewModel,
                onNavigateToDetail = onNavigateToDetail
            )
            1 -> HexagramQueryScreen(
                viewModel = viewModel,
                autoOpenId = pendingDetailId,
                onAutoOpenConsumed = onAutoOpenConsumed
            )
        }
    }
}

@Composable
private fun RowScope.NavigationBarItems(currentTab: Int, onSelect: (Int) -> Unit) {
    NavigationBarItem(
        selected = currentTab == 0,
        onClick = { onSelect(0) },
        icon = { Icon(Icons.Default.Star, contentDescription = "起卦") },
        label = { Text("起卦") }
    )
    NavigationBarItem(
        selected = currentTab == 1,
        onClick = { onSelect(1) },
        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "64卦庫") },
        label = { Text("64卦庫") }
    )
}

@Composable
private fun NavigationRailItems(currentTab: Int, onSelect: (Int) -> Unit) {
    NavigationRailItem(
        selected = currentTab == 0,
        onClick = { onSelect(0) },
        icon = { Icon(Icons.Default.Star, contentDescription = "起卦") },
        label = { Text("起卦") }
    )
    NavigationRailItem(
        selected = currentTab == 1,
        onClick = { onSelect(1) },
        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "64卦庫") },
        label = { Text("64卦庫") }
    )
}
