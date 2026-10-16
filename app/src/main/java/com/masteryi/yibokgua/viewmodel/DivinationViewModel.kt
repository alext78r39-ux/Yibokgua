package com.masteryi.yibokgua.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masteryi.core.model.iching.EightPalaceBuilder
import com.masteryi.core.model.iching.EightPalaceGroup
import com.masteryi.core.model.iching.DivinationResult
import com.masteryi.core.model.iching.Hexagram
import com.masteryi.core.model.iching.TiYongAnalysis
import com.masteryi.core.model.iching.Trigram
import com.masteryi.core.model.iching.TrigramInfo
import com.masteryi.core.model.iching.Yao
import com.masteryi.data.repository.HexagramRepository
import com.masteryi.data.repository.TrigramInfoRepository
import com.masteryi.engine.iching.DivinationEngine
import com.masteryi.engine.iching.TiYongAnalyzer
import com.masteryi.engine.iching.countChineseCharacters
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DivinationUiState(
    val currentYaos: List<Yao> = emptyList(), // 手動/快速搖卦當前累積或生成的爻 (初爻 -> 上爻)
    val autoShakeProgress: Float = 0f,        // 長按太極進度 (0.0 ~ 1.0)
    val isShaking: Boolean = false,
    val result: DivinationResult? = null,
    val tiYongAnalysis: TiYongAnalysis? = null, // 體用生克分析
    val question: String = "",                // 所占之事（首頁輸入）
    val historyList: List<DivinationResult> = emptyList()
)

class DivinationViewModel : ViewModel() {

    private val repository = HexagramRepository()
    private val trigramInfoRepository = TrigramInfoRepository()
    private val engine = DivinationEngine(findHexagramByBinary = repository::getHexagramByBinary)
    private val tiYongAnalyzer = TiYongAnalyzer(findHexagramByBinary = repository::getHexagramByBinary)

    private val _uiState = MutableStateFlow(DivinationUiState())
    val uiState: StateFlow<DivinationUiState> = _uiState.asStateFlow()

    private var longPressJob: Job? = null

    // 1. 時間起卦（國曆）
    fun performTimeDivination(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        val result = engine.calculateTimeDivination(year, month, day, hour, minute)
        setResult(result)
    }

    // 1b. 時間起卦（農曆・梅花易數）
    fun performLunarTimeDivination(
        yearBranchNum: Int,
        lunarMonth: Int,
        lunarDay: Int,
        hourBranchNum: Int
    ) {
        val result = engine.calculateLunarTimeDivination(
            yearBranchNum, lunarMonth, lunarDay, hourBranchNum
        )
        setResult(result)
    }

    // 2. 手動搖卦：太極按鈕長按控制 (2 秒生成一爻)
    fun startTaijiLongPress() {
        if (_uiState.value.currentYaos.size >= 6) return
        longPressJob?.cancel()

        longPressJob = viewModelScope.launch {
            _uiState.update { it.copy(isShaking = true, autoShakeProgress = 0f) }
            val totalTimeMs = 2000L
            val intervalMs = 50L
            var elapsed = 0L

            while (elapsed < totalTimeMs) {
                delay(intervalMs)
                elapsed += intervalMs
                _uiState.update { it.copy(autoShakeProgress = elapsed.toFloat() / totalTimeMs) }
            }

            // 完成 2 秒長按，生成一爻
            val newYao = engine.rollSingleYao()
            val updatedYaos = _uiState.value.currentYaos + newYao

            _uiState.update {
                it.copy(
                    currentYaos = updatedYaos,
                    autoShakeProgress = 0f,
                    isShaking = false
                )
            }

            // 若滿 6 爻自動結算
            if (updatedYaos.size == 6) {
                val result = engine.buildResultFrom6Yaos(updatedYaos, "手動搖卦")
                setResult(result)
            }
        }
    }

    fun cancelTaijiLongPress() {
        longPressJob?.cancel()
        _uiState.update { it.copy(isShaking = false, autoShakeProgress = 0f) }
    }

    // 3. 快速起卦：一鍵生成六個爻
    fun performQuickDivination() {
        if (_uiState.value.isShaking) return
        val newYaos = List(6) { engine.rollSingleYao() }
        _uiState.update { it.copy(currentYaos = newYaos, result = null) }
        val result = engine.buildResultFrom6Yaos(newYaos, "快速起卦")
        setResult(result)
    }

    // 4. 類象加方位起卦
    fun performCategoryDirectionDivination(categoryNum: Int, directionNum: Int) {
        val result = engine.calculateCategoryDirectionDivination(categoryNum, directionNum)
        setResult(result)
    }

    // 5. 字占起卦：輸入 1-10 個中文字成卦
    fun validateCharacterInput(text: String): Boolean =
        countChineseCharacters(text) in 1..10

    fun performCharacterDivination(text: String): Boolean {
        if (!validateCharacterInput(text)) return false
        val result = engine.calculateCharacterDivination(text).copy(divinationText = text)
        setResult(result)
        return true
    }

    fun resetShake() {
        _uiState.update { it.copy(currentYaos = emptyList(), result = null) }
    }

    // 首頁設定所占之事，後續起卦結果會自動帶入歷史紀錄
    fun setQuestion(question: String) {
        _uiState.update { it.copy(question = question.trim()) }
    }

    private fun setResult(result: DivinationResult) {
        val question = _uiState.value.question
        val withQuestion = if (question.isBlank()) result else result.copy(question = question)
        _uiState.update {
            it.copy(
                result = withQuestion,
                tiYongAnalysis = tiYongAnalyzer.analyze(withQuestion),
                historyList = listOf(withQuestion) + it.historyList
            )
        }
    }

    // 供歷史紀錄導出詳情卡片時重算體用生克
    fun analyzeResult(result: DivinationResult): TiYongAnalysis = tiYongAnalyzer.analyze(result)

    fun getAll64Hexagrams(): List<Hexagram> = repository.getAllHexagrams()

    fun getHexagramById(id: Int): Hexagram? = repository.getHexagramById(id)

    private val eightPalacesData: List<EightPalaceGroup> by lazy { EightPalaceBuilder.build(repository.getAllHexagrams()) }
    fun getEightPalaces(): List<EightPalaceGroup> = eightPalacesData

    // 八卦資料
    fun getTrigramInfo(trigram: Trigram): TrigramInfo = trigramInfoRepository.getTrigramInfo(trigram)
    fun getAllTrigramInfos(): List<TrigramInfo> = trigramInfoRepository.getAll()
}
