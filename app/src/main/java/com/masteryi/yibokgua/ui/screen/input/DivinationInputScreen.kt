package com.masteryi.yibokgua.ui.screen.input

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.masteryi.core.model.iching.DivinationResult
import com.masteryi.core.model.iching.Hexagram
import com.masteryi.core.model.iching.TiYongAnalysis
import com.masteryi.core.model.iching.Yao
import com.masteryi.yibokgua.viewmodel.DivinationViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 全局統一細框線樣式（隨主題自適應） */
private val ThinBorder: BorderStroke
    @Composable get() = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)

/** 日期顯示格式 */
private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日")

@Composable
fun DivinationInputScreen(
    viewModel: DivinationViewModel,
    onNavigateToDetail: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("自動", "手動", "日期", "類象")

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
                0 -> AutoShakeView(viewModel = viewModel)
                1 -> ManualShakeView(viewModel = viewModel)
                2 -> TimeDivinationView(onCalculate = viewModel::performTimeDivination)
                3 -> CategoryDirectionView(onCalculate = viewModel::performCategoryDirectionDivination)
            }
        }

        // 卦象結果展示區
        uiState.result?.let { result ->
            DivinationResultCard(
                result = result,
                tiYongAnalysis = uiState.tiYongAnalysis,
                onNavigateToDetail = onNavigateToDetail,
                onReset = viewModel::resetShake
            )
        }
    }
}

/** 自動搖卦 - 太極圖長按元件（依可用空間自適應尺寸，空間不足時自動改為捲動佈局） */
@Composable
fun AutoShakeView(viewModel: DivinationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var started by remember { mutableStateOf(false) }

    if (!started) {
        ShakeStartPrompt(
            title = "自動搖卦",
            description = "點擊「開始」後，長按太極圖 2 秒生成一爻，依序搖滿六爻成卦。",
            showTaiji = true,
            onStart = { started = true }
        )
        return
    }

    val progressAnimated by animateFloatAsState(targetValue = uiState.autoShakeProgress, label = "progress")
    val hint = if (uiState.currentYaos.size < 6) "長按太極圖 2 秒生成一爻 (${uiState.currentYaos.size}/6)" else "起卦完成！"

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxHeight < 460.dp
        val taijiSize = minOf(160.dp, maxWidth * 0.5f, maxHeight * 0.40f).coerceAtLeast(96.dp)

        AutoShakeLayout(
            isCompact = isCompact,
            yaos = uiState.currentYaos,
            hint = hint,
            progress = progressAnimated,
            taijiSize = taijiSize,
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
    onPressStart: () -> Unit,
    onPressRelease: () -> Unit,
    onReset: () -> Unit
) {
    val content: @Composable ColumnScope.() -> Unit = {
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
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showTaiji) {
            TaijiArtwork(modifier = Modifier.size(160.dp))
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
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("開始", style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** 繪製 6 爻看板 (由下至上：初爻為底部，第 0 項繪製於最下方) */
@Composable
fun YaoDisplayBoard(yaos: List<Yao>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = ThinBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
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

/** 時間起卦介面（日期選擇器 + 24 小時制時間滾輪選擇） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDivinationView(onCalculate: (Int, Int, Int, Int, Int) -> Unit) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var hour by remember { mutableIntStateOf(LocalTime.now().hour) }
    var minute by remember { mutableIntStateOf(LocalTime.now().minute) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("請選擇日期與時間（梅花易數）：", style = MaterialTheme.typography.titleSmall)

        // 日期選擇欄位
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            border = ThinBorder
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
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            border = ThinBorder
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
            Text("梅花起卦")
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
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
                shape = RoundedCornerShape(24.dp),
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

/** 手動搖卦介面（依可用空間自適應，空間不足時自動改為捲動佈局） */
@Composable
fun ManualShakeView(viewModel: DivinationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var started by remember { mutableStateOf(false) }

    if (!started) {
        ShakeStartPrompt(
            title = "手動搖卦",
            description = "點擊「開始」後，依序點選爻類型（6 老陰・7 少陽・8 少陰・9 老陽），搖滿六爻成卦。",
            onStart = { started = true }
        )
        return
    }

    // 數字 爻象名 爻線圖示 變爻標記
    val manualOptions = listOf(
        Yao.OLD_YIN to "╍ ◯",       // 6 老陰
        Yao.YOUNG_YANG to "━",        // 7 少陽
        Yao.YOUNG_YIN to "╍",         // 8 少陰
        Yao.OLD_YANG to "━ ✕"         // 9 老陽
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxHeight < 560.dp

        val content: @Composable ColumnScope.() -> Unit = {
            YaoDisplayBoard(yaos = uiState.currentYaos, modifier = Modifier.fillMaxWidth())

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "點擊下方選擇爻類型（由初爻依次指定至上爻）：",
                    style = MaterialTheme.typography.titleSmall
                )

                manualOptions.forEach { (yao, glyph) ->
                    val isMoving = yao.isMoving
                    Button(
                        onClick = { viewModel.addManualYao(yao) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMoving) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                            contentColor = if (isMoving) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${yao.code} ${yao.label}",
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isMoving) {
                                    Text(
                                        text = "變爻 ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Text(
                                    text = glyph,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.resetShake()
                    started = false
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("重置")
            }
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
}

/** 類象方位起卦介面 */
@Composable
fun CategoryDirectionView(onCalculate: (Int, Int) -> Unit) {
    var categoryNum by remember { mutableStateOf("") }
    var directionNum by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("類象加方位起卦：")
        OutlinedTextField(value = categoryNum, onValueChange = { categoryNum = it }, label = { Text("物象/觀察數") })
        OutlinedTextField(value = directionNum, onValueChange = { directionNum = it }, label = { Text("方位數") })
        Button(
            onClick = {
                val c = categoryNum.toIntOrNull() ?: 1
                val d = directionNum.toIntOrNull() ?: 1
                onCalculate(c, d)
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
    onNavigateToDetail: (Int) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = ThinBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
}

/** 體用生克分析區塊（判斷吉凶的核心） */
@Composable
private fun TiYongAnalysisSection(tiYongAnalysis: TiYongAnalysis?) {
    if (tiYongAnalysis == null) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = ThinBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
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
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = ThinBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
