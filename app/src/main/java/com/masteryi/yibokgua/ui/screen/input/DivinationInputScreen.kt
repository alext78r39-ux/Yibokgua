package com.masteryi.yibokgua.ui.screen.input

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.masteryi.core.model.iching.DivinationResult
import com.masteryi.core.model.iching.Hexagram
import com.masteryi.core.model.iching.LunarDate
import com.masteryi.core.model.iching.TiYongAnalysis
import com.masteryi.core.model.iching.Trigram
import com.masteryi.core.model.iching.TrigramInfo
import com.masteryi.core.model.iching.Yao
import com.masteryi.yibokgua.ui.components.AdaptiveScreen
import com.masteryi.yibokgua.ui.components.TrigramInfoDialog
import com.masteryi.yibokgua.ui.components.TrigramPairSection
import com.masteryi.yibokgua.ui.components.ZenCard
import com.masteryi.yibokgua.ui.components.zenThinBorder
import com.masteryi.yibokgua.ui.theme.LocalKeyboardScale
import com.masteryi.yibokgua.viewmodel.DivinationViewModel
import com.masteryi.engine.iching.EARTHLY_BRANCH_NAMES
import com.masteryi.engine.iching.countChineseCharacters
import com.masteryi.engine.iching.hourBranchNumber
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** 全局統一細框線樣式（隨主題自適應） */
private val ThinBorder: BorderStroke
    @Composable get() = zenThinBorder()

/** 日期顯示格式 */
private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")

@Composable
fun DivinationInputScreen(
    viewModel: DivinationViewModel,
    onNavigateToDetail: (Int) -> Unit,
    requestedTabIndex: Int? = null,
    onRequestedTabConsumed: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("手動", "快速", "日期", "類象", "字占")

    LaunchedEffect(requestedTabIndex) {
        if (requestedTabIndex != null && requestedTabIndex in tabs.indices) {
            selectedTabIndex = requestedTabIndex
            onRequestedTabConsumed()
        }
    }

    AdaptiveScreen {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
            selectedTabIndex = selectedTabIndex,
            divider = { HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index; viewModel.resetShake() },
                    text = { Text(title, fontSize = 14.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> ManualShakeView(viewModel = viewModel)
                1 -> QuickShakeView(viewModel = viewModel)
                2 -> TimeDivinationView(
                    onCalculateSolar = viewModel::performTimeDivination,
                    onCalculateLunar = viewModel::performLunarTimeDivination
                )
                3 -> CategoryDirectionView(onCalculate = viewModel::performCategoryDirectionDivination)
                4 -> CharacterDivinationView(onCalculate = viewModel::performCharacterDivination)
            }
        }

        // 卦象結果展示區
        uiState.result?.let { result ->
                DivinationResultCard(
                    result = result,
                    tiYongAnalysis = uiState.tiYongAnalysis,
                    getTrigramInfo = viewModel::getTrigramInfo,
                    onNavigateToDetail = onNavigateToDetail,
                    onReset = viewModel::resetShake
                )
            }
        }
    }
}

/** 手動搖卦 - 太極圖長按元件（依可用空間自適應尺寸，空間不足時自動改為捲動佈局） */
@Composable
fun ManualShakeView(viewModel: DivinationViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var started by remember { mutableStateOf(false) }

    if (!started) {
        ShakeStartPrompt(
            title = "手動搖卦",
            description = "點擊「開始」後，長按太極圖 2 秒生成一爻，依序搖滿六爻成卦。中途放開可重新計時。",
            showTaiji = true,
            extraMessage = uiState.question,
            onStart = { started = true }
        )
        return
    }

    val progressAnimated by animateFloatAsState(targetValue = uiState.autoShakeProgress, label = "progress")
    val hint = if (uiState.currentYaos.size < 6) "長按太極圖 2 秒生成一爻 (${uiState.currentYaos.size}/6)" else "起卦完成！"

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxHeight < 460.dp
        val keyboardScale = LocalKeyboardScale.current
        val taijiSize = minOf(160.dp * keyboardScale, maxWidth * 0.5f, maxHeight * 0.40f).coerceAtLeast(96.dp)

        AutoShakeLayout(
            isCompact = isCompact,
            yaos = uiState.currentYaos,
            hint = hint,
            progress = progressAnimated,
            taijiSize = taijiSize,
            question = uiState.question,
            onPressStart = viewModel::startTaijiLongPress,
            onPressRelease = viewModel::cancelTaijiLongPress,
            onReset = {
                viewModel.resetShake()
                started = false
            }
        )
    }
}

@Composable
private fun AutoShakeLayout(
    isCompact: Boolean,
    yaos: List<Yao>,
    hint: String,
    progress: Float,
    taijiSize: Dp,
    question: String? = null,
    onPressStart: () -> Unit,
    onPressRelease: () -> Unit,
    onReset: () -> Unit
) {
    val content: @Composable ColumnScope.() -> Unit = {
        if (!question.isNullOrBlank()) {
            QuestionBanner(question)
            Spacer(modifier = Modifier.height(8.dp))
        }
        YaoDisplayBoard(yaos = yaos, modifier = Modifier.fillMaxWidth())
        Text(
            text = hint,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        TaijiButton(
            size = taijiSize,
            progress = progress,
            onPressStart = onPressStart,
            onPressRelease = onPressRelease
        )
        Button(onClick = onReset) { Text("重新搖卦") }
    }

    if (isCompact) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

/** 太極長按按鈕（進度環 + 太極圖，均為相對尺寸） */
@Composable
private fun TaijiButton(
    size: Dp,
    progress: Float,
    onPressStart: () -> Unit,
    onPressRelease: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressStart()
                        tryAwaitRelease()
                        onPressRelease()
                    }
                )
            }
    ) {
        val progressTrackColor = MaterialTheme.colorScheme.outlineVariant
        val progressColor = MaterialTheme.colorScheme.primary

        // 長按進度環
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = progressTrackColor, style = Stroke(width = 8.dp.toPx()))
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx())
            )
        }

        // 太極視覺圖形（隨尺寸自適應）
        TaijiArtwork(modifier = Modifier.fillMaxSize(0.75f))
    }
}

/** 太極視覺圖形（黑白雙魚） */
@Composable
private fun TaijiArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = this.size.minDimension / 2
        drawArc(Color.Black, 90f, 180f, true)
        drawArc(Color.White, -90f, 180f, true)
        drawCircle(Color.White, radius = r / 2, center = Offset(center.x, center.y - r / 2))
        drawCircle(Color.Black, radius = r / 2, center = Offset(center.x, center.y + r / 2))
        drawCircle(Color.Black, radius = r / 8, center = Offset(center.x, center.y - r / 2))
        drawCircle(Color.White, radius = r / 8, center = Offset(center.x, center.y + r / 2))
    }
}

/** 搖卦開始提示畫面：說明操作流程，按下「開始」後才進入完整搖卦流程 */
@Composable
private fun ShakeStartPrompt(
    title: String,
    description: String,
    showTaiji: Boolean = false,
    extraMessage: String? = null,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val keyboardScale = LocalKeyboardScale.current
        if (showTaiji) {
            TaijiArtwork(modifier = Modifier.size(160.dp * keyboardScale))
            Spacer(modifier = Modifier.height(28.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (!extraMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            QuestionBanner(extraMessage, modifier = Modifier.fillMaxWidth())
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp * keyboardScale)
        ) {
            Text("開始", style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** 所占之事提示卡 */
@Composable
private fun QuestionBanner(text: String, modifier: Modifier = Modifier) {
    ZenCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "所占之事：$text",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp)
        )
    }
}

/** 繪製 6 爻看板 (由下至上：初爻為底部，第 0 項繪製於最下方) */
@Composable
fun YaoDisplayBoard(yaos: List<Yao>, modifier: Modifier = Modifier) {
    ZenCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 從上爻(5)繪製到初爻(0)，每爻固定行高確保六爻完整顯示
            for (i in 5 downTo 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                ) {
                    if (i < yaos.size) {
                        val yao = yaos[i]
                        Text(
                            text = "爻 ${i + 1}: ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = yao.symbol,
                            fontSize = 20.sp,
                            color = if (yao.isMoving) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " (${yao.label})",
                            fontSize = 12.sp,
                            color = if (yao.isMoving) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "爻 ${i + 1}: ⚊ ⚊ (待搖)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

/** 時間起卦介面：可切換「農曆（梅花易數）」與「國曆」兩種取數方式 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDivinationView(
    onCalculateSolar: (Int, Int, Int, Int, Int) -> Unit,
    onCalculateLunar: (Int, Int, Int, Int) -> Unit
) {
    var useLunar by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = useLunar,
                onClick = { useLunar = true },
                label = { Text("農曆（梅花易數）") }
            )
            FilterChip(
                selected = !useLunar,
                onClick = { useLunar = false },
                label = { Text("國曆") }
            )
        }

        if (useLunar) {
            LunarTimeDivinationForm(onCalculate = onCalculateLunar)
        } else {
            SolarTimeDivinationForm(onCalculate = onCalculateSolar)
        }
    }
}

/** 農曆時間起卦：輸入年支、農曆月、農曆日、時支 */
@Composable
private fun LunarTimeDivinationForm(onCalculate: (Int, Int, Int, Int) -> Unit) {
    val todayLunar = remember { runCatching { LunarDate.of(LocalDate.now()) }.getOrNull() }
    var yearBranch by remember { mutableIntStateOf(todayLunar?.yearBranchNumber ?: 1) }
    var lunarMonth by remember { mutableIntStateOf(todayLunar?.month ?: 1) }
    var lunarDay by remember { mutableIntStateOf(todayLunar?.day ?: 1) }
    var hourBranch by remember { mutableIntStateOf(hourBranchNumber(LocalTime.now().hour)) }

    Text("梅花易數以「年支、農曆月、農曆日、時支」取數：", style = MaterialTheme.typography.titleSmall)
    Text(
        text = "上卦＝(年支＋月＋日) mod 8；下卦＝(年支＋月＋日＋時支) mod 8；動爻＝同前 mod 6。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    DropdownSelector(
        label = "年支",
        value = "${EARTHLY_BRANCH_NAMES[yearBranch - 1]}（$yearBranch）",
        options = EARTHLY_BRANCH_NAMES,
        onSelect = { yearBranch = it + 1 }
    )
    DropdownSelector(
        label = "農曆月",
        value = "$lunarMonth 月",
        options = (1..12).map { "$it 月" },
        onSelect = { lunarMonth = it + 1 }
    )
    DropdownSelector(
        label = "農曆日",
        value = "$lunarDay 日",
        options = (1..30).map { "$it 日" },
        onSelect = { lunarDay = it + 1 }
    )
    DropdownSelector(
        label = "時支",
        value = "${EARTHLY_BRANCH_NAMES[hourBranch - 1]}（$hourBranch）",
        options = EARTHLY_BRANCH_NAMES,
        onSelect = { hourBranch = it + 1 }
    )

    Button(
        onClick = { onCalculate(yearBranch, lunarMonth, lunarDay, hourBranch) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("梅花起卦")
    }
}

/** 單一選項下拉欄位 */
@Composable
private fun DropdownSelector(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(value)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/** 國曆時間起卦：日期選擇器 + 24 小時制時間滾輪 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SolarTimeDivinationForm(onCalculate: (Int, Int, Int, Int, Int) -> Unit) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var hour by remember { mutableIntStateOf(LocalTime.now().hour) }
    var minute by remember { mutableIntStateOf(LocalTime.now().minute) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("請選擇國曆日期與時間：", style = MaterialTheme.typography.titleSmall)

        // 日期選擇欄位
        ZenCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showDatePicker = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "日期",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = date.format(DATE_FORMAT),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "選擇日期",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 時間選擇欄位（24 小時制）
        ZenCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showTimePicker = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "時間（24 小時制）",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%02d:%02d".format(hour, minute),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "選擇時間",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Button(
            onClick = {
                onCalculate(date.year, date.monthValue, date.dayOfMonth, hour, minute)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("國曆起卦")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            // Material3 DatePicker 以 UTC 表示日期，須以 UTC 轉換以避免時區造成的位移
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("確定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                border = ThinBorder,
                modifier = Modifier.width(328.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("取消") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            hour = timePickerState.hour
                            minute = timePickerState.minute
                            showTimePicker = false
                        }) { Text("確定") }
                    }
                }
            }
        }
    }
}

/** 快速起卦介面：一鍵直接生成六個爻 */
@Composable
fun QuickShakeView(viewModel: DivinationViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var started by remember { mutableStateOf(false) }

    if (!started) {
        ShakeStartPrompt(
            title = "快速起卦",
            description = "點擊「開始」後，直接生成所有六個爻，一鍵成卦。",
            onStart = {
                started = true
                viewModel.performQuickDivination()
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "本次生成的六爻如下：",
            style = MaterialTheme.typography.titleSmall
        )
        YaoDisplayBoard(yaos = uiState.currentYaos, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = viewModel::performQuickDivination,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("重新起卦")
        }
        Button(
            onClick = {
                viewModel.resetShake()
                started = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("重置")
        }
    }
}

/** 字占起卦介面：輸入 1-10 個中文字，送出後成卦 */
@Composable
fun CharacterDivinationView(onCalculate: (String) -> Boolean) {
    var input by rememberSaveable { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("字占起卦：", style = MaterialTheme.typography.titleSmall)
        Text(
            text = "輸入 1-10 個中文字，依其字數與筆畫取數成卦。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
                showError = false
            },
            placeholder = { Text("請輸入所占之字") },
            supportingText = {
                Text("${countChineseCharacters(input)} / 10 字")
            },
            isError = showError,
            modifier = Modifier.fillMaxWidth()
        )
        if (showError) {
            Text(
                text = "請輸入 1-10 個中文字",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        Button(
            onClick = {
                showError = !onCalculate(input)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("字占起卦")
        }
    }
}

/** 類象方位起卦介面 */
@Composable
fun CategoryDirectionView(onCalculate: (Int, Int) -> Unit) {
    var categoryNum by remember { mutableStateOf("") }
    var directionNum by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("類象加方位起卦：")
        OutlinedTextField(
            value = categoryNum,
            onValueChange = { categoryNum = it; error = null },
            label = { Text("物象/觀察數") },
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = directionNum,
            onValueChange = { directionNum = it; error = null },
            label = { Text("方位數") },
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Button(
            onClick = {
                val c = categoryNum.trim().toIntOrNull()
                val d = directionNum.trim().toIntOrNull()
                if (c == null || d == null || c <= 0 || d <= 0) {
                    error = "請輸入 1 以上的正整數"
                } else {
                    onCalculate(c, d)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("生成卦象")
        }
    }
}

/** 起卦結果卡片 */
@Composable
fun DivinationResultCard(
    result: DivinationResult,
    tiYongAnalysis: TiYongAnalysis?,
    getTrigramInfo: (Trigram) -> TrigramInfo,
    onNavigateToDetail: (Int) -> Unit,
    onReset: () -> Unit
) {
    var dialogInfo by remember { mutableStateOf<TrigramInfo?>(null) }

    ZenCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "起卦類型：${result.divinationType}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            result.question.takeIf { it.isNotBlank() }?.let { question ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "所占之事：$question",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            result.divinationText.takeIf { it.isNotBlank() }?.let { text ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "占字：$text",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("點擊卦名可跳轉 64 卦庫查看對應詳情", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            // 全局自適應：寬度不足時改為上下堆疊
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth >= 360.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResultHexagramColumn(
                            label = "本卦",
                            hexagram = result.originalHexagram,
                            onClick = { onNavigateToDetail(result.originalHexagram.id) },
                            modifier = Modifier.weight(1f)
                        )
                        result.mutatedHexagram?.let { mutated ->
                            ResultHexagramColumn(
                                label = "變卦",
                                hexagram = mutated,
                                onClick = { onNavigateToDetail(mutated.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ResultHexagramColumn(
                            label = "本卦",
                            hexagram = result.originalHexagram,
                            onClick = { onNavigateToDetail(result.originalHexagram.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        result.mutatedHexagram?.let { mutated ->
                            ResultHexagramColumn(
                                label = "變卦",
                                hexagram = mutated,
                                onClick = { onNavigateToDetail(mutated.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            result.movingYaoIndex?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "動爻：第 $it 爻",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            // 上下卦八卦資料區塊（本卦；點擊小卡可看單卦詳情）
            Spacer(modifier = Modifier.height(12.dp))
            TrigramPairSection(
                title = "本卦・上下卦資料",
                lower = getTrigramInfo(result.originalHexagram.lowerTrigram),
                upper = getTrigramInfo(result.originalHexagram.upperTrigram),
                onTrigramClick = { dialogInfo = it }
            )

            // 上下卦八卦資料區塊（變卦）
            result.mutatedHexagram?.let { mutated ->
                if (mutated.lowerTrigram != result.originalHexagram.lowerTrigram ||
                    mutated.upperTrigram != result.originalHexagram.upperTrigram
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TrigramPairSection(
                        title = "變卦・上下卦資料",
                        lower = getTrigramInfo(mutated.lowerTrigram),
                        upper = getTrigramInfo(mutated.upperTrigram),
                        onTrigramClick = { dialogInfo = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TiYongAnalysisSection(tiYongAnalysis = tiYongAnalysis)

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("再算一次")
                }
                Button(
                    onClick = { onNavigateToDetail(result.originalHexagram.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("查看 64 卦詳情")
                }
            }
        }
    }

    // 單卦詳情彈窗（點擊上下卦小卡時顯示）
    dialogInfo?.let { info ->
        TrigramInfoDialog(info = info, onDismiss = { dialogInfo = null })
    }
}

/** 體用生克分析區塊（判斷吉凶的核心） */
@Composable
private fun TiYongAnalysisSection(tiYongAnalysis: TiYongAnalysis?) {
    if (tiYongAnalysis == null) return
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "【體用生克】",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "體卦${tiYongAnalysis.bodyTrigram.symbol}${tiYongAnalysis.bodyTrigram.nameZh}為自身，用卦${tiYongAnalysis.appTrigram.symbol}${tiYongAnalysis.appTrigram.nameZh}為所應之事，互卦為事之中間，變卦為事之終。",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(6.dp))
            tiYongAnalysis.summary.split("\n").forEach { line ->
                Text(
                    text = "・$line",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

/** 本卦 / 變卦 展示欄位（可點擊跳轉 64 卦庫） */
@Composable
private fun ResultHexagramColumn(
    label: String,
    hexagram: Hexagram,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ZenCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = hexagram.symbol,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = hexagram.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${hexagram.upperTrigram.symbol} ${hexagram.lowerTrigram.symbol}",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "第 ${hexagram.id} 卦・點擊查看 →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
