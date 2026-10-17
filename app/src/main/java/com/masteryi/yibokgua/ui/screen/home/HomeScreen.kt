package com.masteryi.yibokgua.ui.screen.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.masteryi.core.model.iching.LunarDate
import com.masteryi.core.model.iching.Trigram
import com.masteryi.core.model.iching.TrigramInfo
import com.masteryi.yibokgua.viewmodel.DivinationViewModel
import com.masteryi.yibokgua.ui.components.AdaptiveScreen
import com.masteryi.yibokgua.ui.components.TrigramInfoDialog
import com.masteryi.yibokgua.ui.components.ZenCard
import com.masteryi.yibokgua.ui.components.zenPressScale
import java.time.LocalDate

/** 全屏自適應參數：依可用高度決定是否進入緊湊模式 */
private data class HomeSpace(
    val compact: Boolean,
    val titleFontSize: Int,
    val quotePadding: Dp,
    val taijiSize: Dp,
    val sectionSpacing: Dp,
    val palaceSymbolSize: Int,
    val palaceNameSize: Int
)

@Composable
private fun HomeScreenSpace(maxHeight: Dp): HomeSpace {
    val compact = maxHeight < 720.dp
    return HomeSpace(
        compact = compact,
        titleFontSize = if (compact) 28 else 36,
        quotePadding = if (compact) 8.dp else 14.dp,
        taijiSize = if (compact) 84.dp else 110.dp,
        sectionSpacing = if (compact) 4.dp else 8.dp,
        palaceSymbolSize = if (compact) 17 else 20,
        palaceNameSize = if (compact) 10 else 12
    )
}

@Composable
fun HomeScreen(
    viewModel: DivinationViewModel,
    onStartManualDivination: () -> Unit
) {
    var selectedTrigram by remember { mutableStateOf<Trigram?>(null) }
    var dialogInfo by remember { mutableStateOf<TrigramInfo?>(null) }
    val lunarToday = remember { runCatching { LunarDate.of(LocalDate.now()) }.getOrNull() }

    AdaptiveScreen {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val space = HomeScreenSpace(maxHeight)
            val horizontalPad = if (space.compact) 12.dp else 16.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPad)
            ) {
                Spacer(Modifier.statusBarsPadding())

                HomeHeader(lunarLabel = lunarToday?.text.orEmpty(), space = space)

                Spacer(Modifier.height(space.sectionSpacing))

                // 所占之事輸入框：輸入後按「確定」跳轉手動起卦
                QuestionInputBar(
                    viewModel = viewModel,
                    onConfirm = onStartManualDivination,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(space.sectionSpacing))

                // 先天八卦環陣：太極居中為起卦入口，八卦依方位環繞，點擊彈出該卦資料視窗
                BaguaWheel(
                    selected = selectedTrigram,
                    onSelect = { trigram ->
                        selectedTrigram = trigram
                        dialogInfo = viewModel.getTrigramInfo(trigram)
                    },
                    onTaijiClick = onStartManualDivination,
                    space = space,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(Modifier.height(space.sectionSpacing))
                DivinationQuote(space = space)

                Spacer(Modifier.height(4.dp))
            }
        }
    }

    // 八卦資料彈窗
    dialogInfo?.let { info ->
        TrigramInfoDialog(info = info, onDismiss = { dialogInfo = null })
    }
}

@Composable
private fun QuestionInputBar(
    viewModel: DivinationViewModel,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    var questionInput by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.question) {
        if (uiState.question.isNotEmpty()) questionInput = uiState.question
    }

    ZenCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = questionInput,
                onValueChange = { questionInput = it },
                placeholder = { Text("輸入所占之事，一事一占、心誠則靈。") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    // 清空輸入框亦會清除所占之事，避免舊問題殘留到之後的起卦
                    viewModel.setQuestion(questionInput.trim())
                    onConfirm()
                }
            ) {
                Text("確定")
            }
        }
    }
}

@Composable
private fun HomeHeader(lunarLabel: String, space: HomeSpace) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "易卜",
            fontSize = space.titleFontSize.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 4.sp
        )
        Spacer(Modifier.height(if (space.compact) 1.dp else 4.dp))
        Text(
            lunarLabel,
            style = if (space.compact) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(1.dp))
        Text(
            "一事一卦 · 心誠則靈",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(if (space.compact) 4.dp else 8.dp))
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}

@Composable
private fun DivinationQuote(space: HomeSpace) {
    ZenCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = space.quotePadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("「", fontSize = (if (space.compact) 20 else 24).sp, fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            Text(
                "禍福莫逃乎數，吉凶皆有其機。",
                fontFamily = FontFamily.Serif,
                fontSize = (if (space.compact) 18 else 22).sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 3.dp)
            )
            Text("」", fontSize = (if (space.compact) 20 else 24).sp, fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TaijiAction(space: HomeSpace, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val press = remember { MutableInteractionSource() }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(space.taijiSize)
                .zenPressScale(press)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                .clickable(
                    interactionSource = press,
                    indication = ripple(),
                    onClick = onClick
                )
        ) {
            TaijiArtwork(Modifier.fillMaxSize(0.78f))
        }
        Spacer(Modifier.height(if (space.compact) 2.dp else 4.dp))
        Text(
            "起卦",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TaijiArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val r = size.minDimension / 2
        drawArc(Color.Black, 90f, 180f, true)
        drawArc(Color.White, -90f, 180f, true)
        drawCircle(Color.White, radius = r / 2, center = Offset(center.x, center.y - r / 2))
        drawCircle(Color.Black, radius = r / 2, center = Offset(center.x, center.y + r / 2))
        drawCircle(Color.Black, radius = r / 8, center = Offset(center.x, center.y - r / 2))
        drawCircle(Color.White, radius = r / 8, center = Offset(center.x, center.y + r / 2))
    }
}

/** 先天八卦方位單位向量（y 向下為正）：乾上坤下、離左坎右、兌左上、巽右上、震左下、艮右下 */
private val BAGUA_DIRECTIONS: List<Pair<Trigram, Pair<Float, Float>>> = listOf(
    Trigram.QIAN to (0.00f to -1.00f),
    Trigram.DUI to (-0.71f to -0.71f),
    Trigram.XUN to (0.71f to -0.71f),
    Trigram.LI to (-1.00f to 0.00f),
    Trigram.KAN to (1.00f to 0.00f),
    Trigram.ZHEN to (-0.71f to 0.71f),
    Trigram.GEN to (0.71f to 0.71f),
    Trigram.KUN to (0.00f to 1.00f)
)

/** 八卦環陣：太極圖居中作為手動起卦按鈕，八卦卡片依先天方位以圓形軌道環繞 */
@Composable
private fun BaguaWheel(
    selected: Trigram?,
    onSelect: (Trigram) -> Unit,
    onTaijiClick: () -> Unit,
    space: HomeSpace,
    modifier: Modifier = Modifier
) {
    val cardWidth = if (space.compact) 46.dp else 54.dp
    val cardHeight = if (space.compact) 62.dp else 74.dp

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 圓形軌道半徑：取寬高較小者，扣除卡片尺寸與邊距，避免四周卡片被裁切或貼邊
        val radius = (
            min(
                maxWidth / 2 - cardWidth / 2,
                maxHeight / 2 - cardHeight / 2
            ) - 6.dp
            ).coerceAtLeast(0.dp)
        val ringColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

        // 虛線圓形軌道：串聯八方，襯托環繞布局
        Canvas(modifier = Modifier.size(radius * 2)) {
            drawCircle(
                color = ringColor,
                style = Stroke(
                    width = 0.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
                )
            )
        }

        TaijiAction(space = space, onClick = onTaijiClick)

        BAGUA_DIRECTIONS.forEach { (trigram, dir) ->
            BaguaCard(
                trigram = trigram,
                selected = trigram == selected,
                onClick = { onSelect(trigram) },
                space = space,
                modifier = Modifier
                    .align(Alignment.Center)
                    .absoluteOffset(x = radius * dir.first, y = radius * dir.second)
                    .size(width = cardWidth, height = cardHeight)
            )
        }
    }
}

@Composable
private fun BaguaCard(
    trigram: Trigram,
    selected: Boolean,
    onClick: () -> Unit,
    space: HomeSpace,
    modifier: Modifier = Modifier
) {
    val container = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val content = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    ZenCard(
        modifier = modifier,
        containerColor = container,
        contentColor = content,
        shape = MaterialTheme.shapes.small,
        border = null,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(trigram.symbol, fontSize = space.palaceSymbolSize.sp)
        }
    }
}