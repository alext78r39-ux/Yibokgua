package com.masteryi.yibokgua.ui.screen.reader

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlin.math.roundToInt

/**
 * 書籍畫面。
 *
 * - 未開啟書本時顯示書架（批量匯入、列表、刪除、閱讀進度）
 * - 開啟後顯示閱讀器（txt / md / docx / doc 文字，pdf 逐頁渲染）
 * - 點擊畫面中央切換上下工具列
 * - 全 Compose、DataStore，未使用 XML layout 或 SharedPreferences
 */
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) viewModel.importUris(uris)
    }

    LaunchedEffect(uiState.importSummary) {
        val summary = uiState.importSummary
        if (summary != null) {
            snackbarHostState.showSnackbar(summary)
            viewModel.consumeImportSummary()
        }
    }

    val current = uiState.currentBook
    if (current == null) {
        BookshelfScreen(
            books = uiState.books,
            isImporting = uiState.isImporting,
            snackbarHostState = snackbarHostState,
            onImport = { importLauncher.launch(arrayOf("*/*")) },
            onOpen = viewModel::openBook,
            onDelete = viewModel::deleteBook,
            modifier = modifier
        )
    } else {
        ReadingScreen(
            book = current,
            content = uiState.content,
            isLoading = uiState.isLoading,
            error = uiState.error,
            settings = settings,
            onBack = viewModel::closeBook,
            onFontSizeChange = viewModel::setFontSizeSp,
            onLineHeightChange = viewModel::setLineHeightMultiplier,
            onProgressChange = viewModel::saveProgress,
            modifier = modifier
        )
    }
}

// ---------------------------------------------------------------------------
// 書架
// ---------------------------------------------------------------------------

@Composable
private fun BookshelfScreen(
    books: List<Book>,
    isImporting: Boolean,
    snackbarHostState: SnackbarHostState,
    onImport: () -> Unit,
    onOpen: (Book) -> Unit,
    onDelete: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingDelete by remember { mutableStateOf<Book?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BookshelfTopBar(onImport = onImport, isImporting = isImporting)
            if (isImporting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (books.isEmpty()) {
                ReaderEmpty(onImport = onImport, modifier = Modifier.weight(1f))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(books, key = { it.id }) { book ->
                        BookCard(
                            book = book,
                            onOpen = { onOpen(book) },
                            onDelete = { pendingDelete = book }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }

    pendingDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("刪除書本") },
            text = { Text("確定要從書架刪除「${book.title}」嗎？") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(book)
                    pendingDelete = null
                }) { Text("刪除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun BookshelfTopBar(onImport: () -> Unit, isImporting: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "書架",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onImport, enabled = !isImporting) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("匯入")
            }
        }
    }
}

@Composable
private fun BookCard(book: Book, onOpen: () -> Unit, onDelete: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FormatBadge(book.format)
                Spacer(Modifier.weight(1f))
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "更多",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("刪除") },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            if (book.progress > 0.001f) {
                LinearProgressIndicator(
                    progress = { book.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "已讀 ${(book.progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "尚未閱讀",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${book.format.label} · ${formatSize(book.sizeBytes)} · ${formatDate(book.importedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FormatBadge(format: BookFormat) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = format.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// 閱讀器
// ---------------------------------------------------------------------------

@Composable
private fun ReadingScreen(
    book: Book,
    content: BookContent?,
    isLoading: Boolean,
    error: String?,
    settings: ReaderSettings,
    onBack: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onLineHeightChange: (Float) -> Unit,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var toolbarVisible by rememberSaveable { mutableStateOf(true) }
    val toggleToolbar = { toolbarVisible = !toolbarVisible }

    var progress by remember(book.id) { mutableStateOf(book.progress) }

    val isText = content is BookContent.Text

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        when {
            isLoading -> ReaderLoading()
            error != null -> ReaderError(
                message = error,
                actionLabel = "返回書架",
                onAction = onBack
            )
            content is BookContent.Text -> ReadingTextContent(
                book = book,
                blocks = content.blocks,
                settings = settings,
                onCenterTap = toggleToolbar,
                onProgressChange = { progress = it; onProgressChange(it) }
            )
            content is BookContent.Pdf && content.pageCount > 0 -> ReadingPdfContent(
                book = book,
                pageCount = content.pageCount,
                onCenterTap = toggleToolbar,
                onProgressChange = { progress = it; onProgressChange(it) }
            )
            content is BookContent.Pdf -> ReaderError(
                message = "此 PDF 沒有可顯示的頁面",
                actionLabel = "返回書架",
                onAction = onBack
            )
            else -> ReaderLoading()
        }

        AnimatedVisibility(
            visible = toolbarVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ReadingTopBar(
                title = book.title,
                format = book.format,
                onBack = onBack
            )
        }

        AnimatedVisibility(
            visible = toolbarVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReadingBottomBar(
                settings = settings,
                showTypography = isText,
                progress = progress,
                onFontSizeChange = onFontSizeChange,
                onLineHeightChange = onLineHeightChange
            )
        }
    }
}

@Composable
private fun ReadingTopBar(
    title: String,
    format: BookFormat,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回書架")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            FormatBadge(format)
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun ReadingBottomBar(
    settings: ReaderSettings,
    showTypography: Boolean,
    progress: Float,
    onFontSizeChange: (Float) -> Unit,
    onLineHeightChange: (Float) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "閱讀進度",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${(progress.coerceIn(0f, 1f) * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (showTypography) {
                SettingSliderRow(
                    label = "字級",
                    valueLabel = "${settings.fontSizeSp.roundToInt()}",
                    value = settings.fontSizeSp,
                    valueRange = ReaderSettings.MIN_FONT_SIZE_SP..ReaderSettings.MAX_FONT_SIZE_SP,
                    steps = 23,
                    onValueChange = onFontSizeChange
                )
                SettingSliderRow(
                    label = "行距",
                    valueLabel = String.format("%.1f", settings.lineHeightMultiplier),
                    value = settings.lineHeightMultiplier,
                    valueRange = ReaderSettings.MIN_LINE_HEIGHT..ReaderSettings.MAX_LINE_HEIGHT,
                    steps = 11,
                    onValueChange = onLineHeightChange
                )
            }
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun ReadingTextContent(
    book: Book,
    blocks: List<ReaderBlock>,
    settings: ReaderSettings,
    onCenterTap: () -> Unit,
    onProgressChange: (Float) -> Unit
) {
    val itemCount = blocks.size
    val startIndex = remember(book.id, itemCount) {
        progressToIndex(book.progress, itemCount)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    LaunchedEffect(book.id, itemCount) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .debounce(600L)
            .collect { index -> onProgressChange(indexToProgress(index, itemCount)) }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val width = size.width.toFloat()
                    val height = size.height.toFloat()
                    val inCenterX = offset.x >= width * 0.25f && offset.x <= width * 0.75f
                    val inCenterY = offset.y >= height * 0.25f && offset.y <= height * 0.75f
                    if (inCenterX && inCenterY) onCenterTap()
                }
            },
        contentPadding = PaddingValues(
            start = settings.contentPaddingDp.dp,
            end = settings.contentPaddingDp.dp,
            top = settings.contentPaddingDp.dp + 72.dp,
            bottom = settings.contentPaddingDp.dp + 140.dp
        ),
        verticalArrangement = Arrangement.spacedBy(settings.paragraphSpacingDp.dp)
    ) {
        itemsIndexed(blocks) { _, block ->
            ReaderBlockView(block = block, settings = settings)
        }
    }
}

@Composable
private fun ReaderBlockView(block: ReaderBlock, settings: ReaderSettings, modifier: Modifier = Modifier) {
    val baseSize = settings.fontSizeSp
    val inline = remember(block.text) { inlineMarkdown(block.text) }
    when (block.kind) {
        BlockKind.RULE -> HorizontalDivider(modifier = modifier.padding(vertical = 4.dp))

        BlockKind.CODE -> Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = block.text,
                fontFamily = FontFamily.Monospace,
                fontSize = (baseSize - 2f).coerceAtLeast(10f).sp,
                lineHeight = ((baseSize - 2f).coerceAtLeast(10f) * 1.4f).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(10.dp)
            )
        }

        BlockKind.QUOTE -> Row(
            modifier = modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = inline,
                fontSize = baseSize.sp,
                lineHeight = (baseSize * settings.lineHeightMultiplier).sp,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BlockKind.H1 -> Text(
            text = inline,
            fontSize = (baseSize + 8f).sp,
            lineHeight = ((baseSize + 8f) * 1.3f).sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier.fillMaxWidth()
        )

        BlockKind.H2 -> Text(
            text = inline,
            fontSize = (baseSize + 4f).sp,
            lineHeight = ((baseSize + 4f) * 1.3f).sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier.fillMaxWidth()
        )

        BlockKind.H3 -> Text(
            text = inline,
            fontSize = (baseSize + 2f).sp,
            lineHeight = ((baseSize + 2f) * 1.3f).sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier.fillMaxWidth()
        )

        BlockKind.BULLET, BlockKind.ORDERED -> Text(
            text = inline,
            fontSize = baseSize.sp,
            lineHeight = (baseSize * settings.lineHeightMultiplier).sp,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
        )

        BlockKind.PARAGRAPH -> Text(
            text = inline,
            fontSize = baseSize.sp,
            lineHeight = (baseSize * settings.lineHeightMultiplier).sp,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier.fillMaxWidth()
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun ReadingPdfContent(
    book: Book,
    pageCount: Int,
    onCenterTap: () -> Unit,
    onProgressChange: (Float) -> Unit
) {
    val startIndex = remember(book.id, pageCount) {
        progressToIndex(book.progress, pageCount)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    LaunchedEffect(book.id, pageCount) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .debounce(600L)
            .collect { index -> onProgressChange(indexToProgress(index, pageCount)) }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val width = size.width.toFloat()
                    val height = size.height.toFloat()
                    val inCenterX = offset.x >= width * 0.2f && offset.x <= width * 0.8f
                    val inCenterY = offset.y >= height * 0.25f && offset.y <= height * 0.75f
                    if (inCenterX && inCenterY) onCenterTap()
                }
            },
        contentPadding = PaddingValues(top = 72.dp, bottom = 120.dp, start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(pageCount) { index ->
            PdfPageView(filePath = book.path, pageIndex = index, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ---------------------------------------------------------------------------
// 共用元件
// ---------------------------------------------------------------------------

@Composable
private fun SettingSliderRow(
    label: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(36.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp)
        )
    }
}

@Composable
private fun ReaderEmpty(onImport: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "書架尚無內容",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "支援 PDF、DOC/DOCX、Markdown 與純文字（.txt），可一次匯入多個檔案。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onImport) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("匯入文件")
        }
    }
}

@Composable
private fun ReaderLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ReaderError(message: String, actionLabel: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "讀取失敗",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onAction) { Text(actionLabel) }
    }
}

// ---------------------------------------------------------------------------
// 輔助函式
// ---------------------------------------------------------------------------

private fun progressToIndex(progress: Float, size: Int): Int {
    if (size <= 1) return 0
    return (progress.coerceIn(0f, 1f) * (size - 1)).roundToInt().coerceIn(0, size - 1)
}

private fun indexToProgress(index: Int, size: Int): Float =
    if (size <= 1) 0f else (index.toFloat() / (size - 1)).coerceIn(0f, 1f)

private fun formatSize(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> String.format("%.1f MB", bytes / 1024.0 / 1024.0)
    bytes >= 1024L -> String.format("%.0f KB", bytes / 1024.0)
    else -> "$bytes B"
}

private fun formatDate(epochMillis: Long): String = runCatching {
    java.time.Instant.ofEpochMilli(epochMillis)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}.getOrDefault("")
