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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masteryi.yibokgua.settings.SettingsViewModel
import com.masteryi.yibokgua.ui.screen.home.HomeScreen
import com.masteryi.yibokgua.ui.screen.home.HistoryScreen
import com.masteryi.yibokgua.ui.screen.home.SettingsScreen
import com.masteryi.yibokgua.ui.screen.input.DivinationInputScreen
import com.masteryi.yibokgua.ui.screen.query.HexagramQueryScreen
import com.masteryi.yibokgua.ui.screen.reader.ReaderScreen
import com.masteryi.yibokgua.ui.screen.reader.ReaderViewModel
import com.masteryi.yibokgua.ui.components.BookIcon
import com.masteryi.yibokgua.ui.components.ZenPageTransition
import com.masteryi.yibokgua.ui.theme.LocalBorderWidth
import com.masteryi.yibokgua.ui.theme.LocalKeyboardScale
import com.masteryi.yibokgua.ui.theme.YibokguaTheme
import com.masteryi.yibokgua.viewmodel.DivinationViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DivinationViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val readerViewModel: ReaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalBorderWidth provides settings.borderWidth,
                LocalKeyboardScale provides settings.keyboardScale
            ) {
            YibokguaTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor,
                fontSizeScale = settings.fontSizeScale,
                colorPreset = settings.colorPreset
            ) {
                var currentTab by rememberSaveable { mutableIntStateOf(0) }
                var pendingDetailId by rememberSaveable { mutableStateOf<Int?>(null) }
                var pendingDivinationTab by rememberSaveable { mutableStateOf<Int?>(null) }

                val isWide = LocalConfiguration.current.screenWidthDp >= 600

                if (isWide) {
                    Scaffold { innerPadding ->
                        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                                settingsViewModel = settingsViewModel,
                                readerViewModel = readerViewModel,
                                pendingDetailId = pendingDetailId,
                                pendingDivinationTab = pendingDivinationTab,
                                onNavigateToDetail = { hexagramId ->
                                    pendingDetailId = hexagramId
                                    currentTab = 2
                                },
                                onStartManualDivination = {
                                    pendingDivinationTab = 0
                                    currentTab = 5
                                },
                                onAutoOpenConsumed = { pendingDetailId = null },
                                onRequestedTabConsumed = { pendingDivinationTab = null },
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
                            settingsViewModel = settingsViewModel,
                            readerViewModel = readerViewModel,
                            pendingDetailId = pendingDetailId,
                            pendingDivinationTab = pendingDivinationTab,
                            onNavigateToDetail = { hexagramId ->
                                pendingDetailId = hexagramId
                                currentTab = 2
                            },
                            onStartManualDivination = {
                                pendingDivinationTab = 0
                                currentTab = 5
                            },
                            onAutoOpenConsumed = { pendingDetailId = null },
                            onRequestedTabConsumed = { pendingDivinationTab = null },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
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
    settingsViewModel: SettingsViewModel,
    readerViewModel: ReaderViewModel,
    pendingDetailId: Int?,
    pendingDivinationTab: Int?,
    onNavigateToDetail: (Int) -> Unit,
    onStartManualDivination: () -> Unit,
    onAutoOpenConsumed: () -> Unit,
    onRequestedTabConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        ZenPageTransition(currentPage = currentTab) { page ->
            when (page) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onStartManualDivination = onStartManualDivination
                )
                1 -> ReaderScreen(viewModel = readerViewModel)
                2 -> HexagramQueryScreen(
                    viewModel = viewModel,
                    autoOpenId = pendingDetailId,
                    onAutoOpenConsumed = onAutoOpenConsumed
                )
                3 -> HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = onNavigateToDetail
                )
                4 -> SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    readerViewModel = readerViewModel
                )
                5 -> DivinationInputScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = onNavigateToDetail,
                    requestedTabIndex = pendingDivinationTab,
                    onRequestedTabConsumed = onRequestedTabConsumed
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavigationBarItems(currentTab: Int, onSelect: (Int) -> Unit) {
    NavigationBarItem(
        selected = currentTab == 0,
        onClick = { onSelect(0) },
        icon = { Icon(Icons.Default.Home, contentDescription = "首頁") },
        label = { Text("首頁") }
    )
    NavigationBarItem(
        selected = currentTab == 1,
        onClick = { onSelect(1) },
        icon = { Icon(BookIcon, contentDescription = "書籍") },
        label = { Text("書籍") }
    )
    NavigationBarItem(
        selected = currentTab == 2,
        onClick = { onSelect(2) },
        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "64卦庫") },
        label = { Text("64卦庫") }
    )
    NavigationBarItem(
        selected = currentTab == 3,
        onClick = { onSelect(3) },
        icon = { Icon(Icons.Default.DateRange, contentDescription = "歷史") },
        label = { Text("歷史") }
    )
    NavigationBarItem(
        selected = currentTab == 4,
        onClick = { onSelect(4) },
        icon = { Icon(Icons.Default.Settings, contentDescription = "設定") },
        label = { Text("設定") }
    )
}

@Composable
private fun NavigationRailItems(currentTab: Int, onSelect: (Int) -> Unit) {
    NavigationRailItem(
        selected = currentTab == 0,
        onClick = { onSelect(0) },
        icon = { Icon(Icons.Default.Home, contentDescription = "首頁") },
        label = { Text("首頁") }
    )
    NavigationRailItem(
        selected = currentTab == 1,
        onClick = { onSelect(1) },
        icon = { Icon(BookIcon, contentDescription = "書籍") },
        label = { Text("書籍") }
    )
    NavigationRailItem(
        selected = currentTab == 2,
        onClick = { onSelect(2) },
        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "64卦庫") },
        label = { Text("64卦庫") }
    )
    NavigationRailItem(
        selected = currentTab == 3,
        onClick = { onSelect(3) },
        icon = { Icon(Icons.Default.DateRange, contentDescription = "歷史") },
        label = { Text("歷史") }
    )
    NavigationRailItem(
        selected = currentTab == 4,
        onClick = { onSelect(4) },
        icon = { Icon(Icons.Default.Settings, contentDescription = "設定") },
        label = { Text("設定") }
    )
}
