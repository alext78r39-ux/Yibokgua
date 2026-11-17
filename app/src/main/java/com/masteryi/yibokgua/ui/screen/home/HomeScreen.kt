package com.masteryi.yibokgua.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masteryi.core.model.iching.LunarDate
import com.masteryi.core.model.iching.Trigram
import com.masteryi.core.model.iching.TrigramInfo
import com.masteryi.yibokgua.ui.components.AdaptiveScreen
import com.masteryi.yibokgua.ui.components.TrigramInfoDialog
import com.masteryi.yibokgua.ui.components.ZenCard
import com.masteryi.yibokgua.ui.theme.LocalYiType
import com.masteryi.yibokgua.ui.theme.YiColors
import com.masteryi.yibokgua.ui.theme.YiMotion
import com.masteryi.yibokgua.ui.theme.YiShape
import com.masteryi.yibokgua.ui.theme.YiStroke
import com.masteryi.yibokgua.ui.theme.YiType
import com.masteryi.yibokgua.viewmodel.DivinationViewModel
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 首頁自適應參數：依可用高度與字級決定尺寸。 */
private data class HomeSpace(
    val compact: Boolean,
    val largeFont: Boolean,
    val taijiSize: Dp,
    val sectionSpacing: Dp,
    val cardWidth: Dp,
    val cardHeight: Dp
)

/** 首頁起卦動畫的即時數值。 */
private data class HomeAnimation(
    val taijiScale: Float,
    val ringRotation: Float,
    val ripple1: Float,
    val ripple2: Float,
    val blooming: Boolean,
    val interactive: Boolean
)

@Composable
private fun homeSpace(maxHeight: Dp): HomeSpace {
    val compact = maxHeight < 720.dp
    val largeFont = LocalDensity.current.fontScale > 1.2f
    return HomeSpace(
        compact = compact,
        largeFont = largeFont,
        taijiSize = if (compact) 84.dp else 110.dp,
        sectionSpacing = if (compact) 6.dp else 10.dp,
        cardWidth = when {
            largeFont -> 40.dp
            compact -> 46.dp
            else -> 54.dp
        },
        cardHeight = when {
            largeFont -> 54.dp
            compact -> 62.dp
            else -> 74.dp
        }
    )
}

@Composable
fun HomeScreen(
    viewModel: DivinationViewModel,
    onStartManualDivination: () -> Unit
) {
    var selectedTrigram by remember { mutableStateOf<Trigram?>(null) }
    var dialogInfo by remember { mutableStateOf<TrigramInfo?>(null) }
    var questionInput by rememberSaveable { mutableStateOf("") }
    var isAnimating by remember { mutableStateOf(false) }
    var emptyHintVisible by remember { mutableStateOf(false) }
    var bloomTarget by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lunarToday = remember { runCatching { LunarDate.of(LocalDate.now()) }.getOrNull() }
    val scope = rememberCoroutineScope()

    val taijiScale = remember { Animatable(1f) }
    val ringRotation = remember { Animatable(0f) }
    val ripple1 = remember { Animatable(0f) }
    val ripple2 = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(uiState.question) {
        if (uiState.question.isNotEmpty()) questionInput = uiState.question
    }

    val onTaijiClick: () -> Unit = {
        if (!isAnimating && questionInput.isBlank()) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val startDivination: () -> Unit = {
        if (!isAnimating) {
            if (questionInput.isBlank()) {
                isAnimating = true
                scope.launch {
                    try {
                        launch {
                            taijiScale.animateTo(0.94f, tween(75, easing = YiMotion.emphasized))
                            taijiScale.animateTo(1f, tween(75, easing = YiMotion.emphasized))
                        }
                        launch {
                            shake.animateTo(1f, tween(80, easing = YiMotion.emphasized))
                            shake.animateTo(-1f, tween(80, easing = YiMotion.emphasized))
                            shake.animateTo(0.6f, tween(60, easing = YiMotion.emphasized))
                            shake.animateTo(-0.6f, tween(60, easing = YiMotion.emphasized))
                            shake.animateTo(0f, tween(60, easing = YiMotion.emphasized))
                        }
                        emptyHintVisible = true
                        focusRequester.requestFocus()
                        keyboardController?.show()
                        delay(320)
                    } finally {
                        isAnimating = false
                    }
                    delay(1180)
                    emptyHintVisible = false
                }
            } else {
                isAnimating = true
                emptyHintVisible = false
                viewModel.setQuestion(questionInput.trim())
                scope.launch {
                    try {
                        taijiScale.snapTo(1f)
                        ringRotation.snapTo(0f)
                        ripple1.snapTo(0f)
                        ripple2.snapTo(0f)
                        bloomTarget = false

                        launch { ripple1.animateTo(1f, tween(300, easing = YiMotion.emphasized)) }
                        taijiScale.animateTo(0.94f, tween(150, easing = YiMotion.emphasized))

                        launch { ringRotation.animateTo(15f, tween(300, easing = YiMotion.emphasized)) }
                        delay(150)
                        bloomTarget = true

                        delay(300)
                        launch { ripple2.animateTo(1f, tween(150, easing = YiMotion.emphasized)) }
                        taijiScale.animateTo(1f, tween(150, easing = YiMotion.emphasized))

                        delay(150)
                        onStartManualDivination()
                        delay(450)
                    } finally {
                        isAnimating = false
                        bloomTarget = false
                        ringRotation.snapTo(0f)
                        taijiScale.snapTo(1f)
                        ripple1.snapTo(0f)
                        ripple2.snapTo(0f)
                    }
                }
            }
        }
    }

    val animation = HomeAnimation(
        taijiScale = taijiScale.value,
        ringRotation = ringRotation.value,
        ripple1 = ripple1.value,
        ripple2 = ripple2.value,
        blooming = bloomTarget,
        interactive = !isAnimating
    )

    AdaptiveScreen {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(YiColors.paper)
        ) {
            val space = homeSpace(maxHeight)
            val horizontalPad = if (space.compact) 12.dp else 16.dp
            val useScroll = space.largeFont && maxHeight < 640.dp
            val contentModifier = Modifier
                .fillMaxSize()
                .imePadding()
                .statusBarsPadding()
                .padding(horizontal = horizontalPad)

            if (useScroll) {
                Column(modifier = contentModifier.verticalScroll(rememberScrollState())) {
                    HomeHeader(lunarLabel = lunarToday?.text.orEmpty(), space = space)
                    Spacer(Modifier.height(space.sectionSpacing))
                    BaguaWheel(
                        selected = selectedTrigram,
                        onSelect = { trigram ->
                            if (!isAnimating) {
                                selectedTrigram = trigram
                                dialogInfo = viewModel.getTrigramInfo(trigram)
                            }
                        },
                        onTaijiClick = onTaijiClick,
                        space = space,
                        animation = animation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    )
                    Spacer(Modifier.height(space.sectionSpacing))
                    QuestionInputBar(
                        value = questionInput,
                        onValueChange = {
                            questionInput = it
                            if (it.isNotBlank()) emptyHintVisible = false
                        },
                        focusRequester = focusRequester,
                        isAnimating = isAnimating,
                        shake = shake.value,
                        hintVisible = emptyHintVisible,
                        onStart = startDivination,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }
            } else {
                Column(modifier = contentModifier) {
                    HomeHeader(lunarLabel = lunarToday?.text.orEmpty(), space = space)
                    Spacer(Modifier.height(space.sectionSpacing))
                    BaguaWheel(
                        selected = selectedTrigram,
                        onSelect = { trigram ->
                            if (!isAnimating) {
                                selectedTrigram = trigram
                                dialogInfo = viewModel.getTrigramInfo(trigram)
                            }
                        },
                        onTaijiClick = onTaijiClick,
                        space = space,
                        animation = animation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Spacer(Modifier.height(space.sectionSpacing))
                    QuestionInputBar(
                        value = questionInput,
                        onValueChange = {
                            questionInput = it
                            if (it.isNotBlank()) emptyHintVisible = false
                        },
                        focusRequester = focusRequester,
                        isAnimating = isAnimating,
                        shake = shake.value,
                        hintVisible = emptyHintVisible,
                        onStart = startDivination,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(if (space.compact) 8.dp else 12.dp))
                }
            }
        }
    }

    dialogInfo?.let { info ->
        TrigramInfoDialog(info = info, onDismiss = { dialogInfo = null })
    }
}

@Composable
private fun HomeHeader(lunarLabel: String, space: HomeSpace) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "易卜",
                style = LocalYiType.current.display,
                color = YiColors.ink
            )
            if (lunarLabel.isNotBlank()) {
                Text(
                    text = " · ",
                    style = LocalYiType.current.caption,
                    color = YiColors.inkTertiary
                )
                Text(
                    text = lunarLabel,
                    style = LocalYiType.current.caption,
                    color = YiColors.inkSecondary
                )
            }
        }
        if (!space.largeFont) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = "一事一卦 · 心誠則靈",
                style = LocalYiType.current.caption,
                color = YiColors.inkSecondary
            )
        }
        Spacer(Modifier.height(if (space.compact) 6.dp else 10.dp))
        HeaderDivider()
    }
}

@Composable
private fun HeaderDivider(modifier: Modifier = Modifier) {
    val hairlineColor = YiColors.hairline
    val cinnabarColor = YiColors.cinnabar
    Canvas(
        modifier = modifier
            .fillMaxWidth(0.6f)
            .height(4.dp)
    ) {
        val dotRadius = 2.dp.toPx()
        val y = size.height / 2f
        drawLine(
            color = hairlineColor,
            start = Offset(dotRadius, y),
            end = Offset(size.width, y),
            strokeWidth = YiStroke.hairlineWidth.toPx()
        )
        drawCircle(
            color = cinnabarColor,
            radius = dotRadius,
            center = Offset(dotRadius, y)
        )
    }
}

@Composable
private fun QuestionInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    isAnimating: Boolean,
    shake: Float,
    hintVisible: Boolean,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        ZenCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            text = "輸入所占之事…",
                            style = LocalYiType.current.body,
                            color = YiColors.inkTertiary
                        )
                    },
                    singleLine = true,
                    textStyle = LocalYiType.current.body,
                    shape = YiShape.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YiColors.ink,
                        unfocusedTextColor = YiColors.ink,
                        cursorColor = YiColors.ink,
                        focusedBorderColor = YiColors.ink,
                        unfocusedBorderColor = YiColors.hairline,
                        focusedContainerColor = YiColors.paperElevated,
                        unfocusedContainerColor = YiColors.paperElevated,
                        focusedPlaceholderColor = YiColors.inkTertiary,
                        unfocusedPlaceholderColor = YiColors.inkTertiary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                )
                StartButton(
                    visualEnabled = value.isNotBlank(),
                    interactionEnabled = !isAnimating,
                    shake = shake,
                    onClick = onStart
                )
            }
        }

        AnimatedVisibility(
            visible = hintVisible,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = (-32).dp),
            enter = fadeIn(tween(YiMotion.short, easing = YiMotion.standard)),
            exit = fadeOut(tween(YiMotion.short, easing = YiMotion.standard))
        ) {
            Text(
                text = "請先輸入所占之事",
                style = LocalYiType.current.caption,
                color = YiColors.inkSecondary
            )
        }
    }
}

@Composable
private fun StartButton(
    visualEnabled: Boolean,
    interactionEnabled: Boolean,
    shake: Float,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(YiMotion.short, easing = YiMotion.standard),
        label = "startButtonScale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                translationX = shake * 8.dp.toPx()
            }
            .clip(YiShape.pill)
            .background(if (visualEnabled) YiColors.cinnabar else YiColors.inkTertiary)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = YiColors.cinnabarSoft),
                enabled = interactionEnabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "起卦",
            style = LocalYiType.current.body,
            color = YiColors.paper,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TaijiAction(
    size: Dp,
    scale: Float,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(YiColors.yang)
            .border(YiStroke.hairlineWidth, YiColors.hairline, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = YiColors.cinnabarSoft),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        TaijiArtwork(Modifier.fillMaxSize(0.78f))
    }
}

@Composable
private fun TaijiWithRipple(
    size: Dp,
    animation: HomeAnimation,
    onClick: () -> Unit
) {
    Box(contentAlignment = Alignment.Center) {
        RippleCircle(progress = animation.ripple1, baseSize = size, maxExtra = 32.dp)
        RippleCircle(progress = animation.ripple2, baseSize = size, maxExtra = 40.dp)
        TaijiAction(
            size = size,
            scale = animation.taijiScale,
            enabled = animation.interactive,
            onClick = onClick
        )
    }
}

@Composable
private fun RippleCircle(
    progress: Float,
    baseSize: Dp,
    maxExtra: Dp
) {
    if (progress <= 0f) return
    val rippleColor = YiColors.cinnabarSoft
    Canvas(Modifier.size(baseSize)) {
        drawCircle(
            color = rippleColor.copy(alpha = rippleColor.alpha * (1f - progress)),
            radius = baseSize.toPx() / 2f + maxExtra.toPx() * progress,
            style = Stroke(width = YiStroke.hairlineWidth.toPx())
        )
    }
}

@Composable
private fun TaijiArtwork(modifier: Modifier = Modifier) {
    val ink = YiColors.ink
    val yang = YiColors.yang
    Canvas(modifier) {
        val r = size.minDimension / 2
        drawArc(ink, 90f, 180f, true)
        drawArc(yang, -90f, 180f, true)
        drawCircle(yang, radius = r / 2, center = Offset(center.x, center.y - r / 2))
        drawCircle(ink, radius = r / 2, center = Offset(center.x, center.y + r / 2))
        drawCircle(ink, radius = r / 8, center = Offset(center.x, center.y - r / 2))
        drawCircle(yang, radius = r / 8, center = Offset(center.x, center.y + r / 2))
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

/** 八卦環陣：太極居中，八卦依先天方位環繞；空間不足時降級為兩行四列格狀。 */
@Composable
private fun BaguaWheel(
    selected: Trigram?,
    onSelect: (Trigram) -> Unit,
    onTaijiClick: () -> Unit,
    space: HomeSpace,
    animation: HomeAnimation,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val rawRadius = (
            minOf(
                maxWidth / 2 - space.cardWidth / 2,
                maxHeight / 2 - space.cardHeight / 2
            ) - 6.dp
            ).coerceAtLeast(0.dp)
        val useGrid = rawRadius < 64.dp
        val radius = maxOf(rawRadius, 64.dp)

        if (useGrid) {
            val rows = BAGUA_DIRECTIONS.chunked(4)
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TaijiWithRipple(
                    size = space.taijiSize,
                    animation = animation,
                    onClick = onTaijiClick
                )
                Spacer(Modifier.height(12.dp))
                rows.forEachIndexed { index, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (trigram, _) ->
                            BaguaCard(
                                trigram = trigram,
                                selected = trigram == selected,
                                onClick = { onSelect(trigram) },
                                enabled = animation.interactive,
                                space = space,
                                modifier = Modifier.size(
                                    width = space.cardWidth,
                                    height = space.cardHeight
                                )
                            )
                        }
                    }
                    if (index != rows.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        } else {
            val ringStroke = YiStroke.dashed()
            val ringColor = YiColors.hairlineFaint
            Canvas(
                modifier = Modifier
                    .size(radius * 2)
                    .graphicsLayer { rotationZ = animation.ringRotation }
            ) {
                drawCircle(
                    color = ringColor,
                    style = ringStroke
                )
            }
            TaijiWithRipple(
                size = space.taijiSize,
                animation = animation,
                onClick = onTaijiClick
            )
            BAGUA_DIRECTIONS.forEachIndexed { index, (trigram, dir) ->
                BaguaCard(
                    trigram = trigram,
                    selected = trigram == selected,
                    onClick = { onSelect(trigram) },
                    enabled = animation.interactive,
                    space = space,
                    blooming = animation.blooming,
                    bloomDirection = dir,
                    bloomDelayMillis = index * 30,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .absoluteOffset(x = radius * dir.first, y = radius * dir.second)
                        .size(width = space.cardWidth, height = space.cardHeight)
                )
            }
        }
    }
}

@Composable
private fun BaguaCard(
    trigram: Trigram,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    space: HomeSpace,
    modifier: Modifier = Modifier,
    blooming: Boolean = false,
    bloomDirection: Pair<Float, Float>? = null,
    bloomDelayMillis: Int = 0
) {
    val selectedScale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = tween(YiMotion.short, easing = YiMotion.emphasized),
        label = "baguaSelectedScale"
    )
    val lift by animateFloatAsState(
        targetValue = if (selected) 2f else 0f,
        animationSpec = tween(YiMotion.short, easing = YiMotion.emphasized),
        label = "baguaSelectedLift"
    )
    val bloomSpec = if (blooming) {
        tween<Float>(
            durationMillis = YiMotion.medium,
            delayMillis = bloomDelayMillis,
            easing = YiMotion.emphasized
        )
    } else {
        tween<Float>(durationMillis = YiMotion.medium, easing = YiMotion.emphasized)
    }
    val bloom by animateFloatAsState(
        targetValue = if (blooming) 1f else 0f,
        animationSpec = bloomSpec,
        label = "baguaBloom"
    )

    ZenCard(
        modifier = modifier.graphicsLayer {
            scaleX = selectedScale
            scaleY = selectedScale
            translationX = bloom * 2.dp.toPx() * (bloomDirection?.first ?: 0f)
            translationY = -lift.dp.toPx() + bloom * 2.dp.toPx() * (bloomDirection?.second ?: 0f)
        },
        shape = YiShape.small,
        selected = selected,
        enabled = enabled,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = trigram.symbol,
                style = if (space.compact || space.largeFont) {
                    LocalYiType.current.subtitle
                } else {
                    LocalYiType.current.heading
                },
                fontFamily = YiType.symbolFamily,
                color = YiColors.ink
            )
        }
    }
}
