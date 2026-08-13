package com.masteryi.yibokgua.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masteryi.core.model.iching.DivinationResult
import com.masteryi.core.model.iching.Hexagram
import com.masteryi.core.model.iching.TiYongAnalysis
import com.masteryi.core.model.iching.Yao
import com.masteryi.data.repository.HexagramRepository
import com.masteryi.engine.iching.DivinationEngine
import com.masteryi.engine.iching.TiYongAnalyzer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DivinationUiState(
    val currentYaos: List<Yao> = emptyList(), // 自動/手動搖卦當前累積的爻 (初爻 -> 上爻)
    val autoShakeProgress: Float = 0f,        // 長按太極進度 (0.0 ~ 1.0)
    val isShaking: Boolean = false,
    val result: DivinationResult? = null,
    val tiYongAnalysis: TiYongAnalysis? = null, // 體用生克分析
    val historyList: List<DivinationResult> = emptyList()
)

class DivinationViewModel : ViewModel() {

    private val repository = HexagramRepository()
    private val engine = DivinationEngine(findHexagramByBinary = repository::getHexagramByBinary)
    private val tiYongAnalyzer = TiYongAnalyzer(findHexagramByBinary = repository::getHexagramByBinary)

    private val _uiState = MutableStateFlow(DivinationUiState())
    val uiState: StateFlow<DivinationUiState> = _uiState.asStateFlow()

    private var longPressJob: Job? = null

    // 1. 時間起卦
    fun performTimeDivination(year: Int, month: Int, day: Int, hour: Int, minute: Int) {
        val result = engine.calculateTimeDivination(year, month, day, hour, minute)
        setResult(result)
    }

    // 2. 自動搖卦：太極按鈕長按控制 (2 秒生成一爻)
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
            val newYao = engine.rollSingleYaoAuto()
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
                val result = engine.buildResultFrom6Yaos(updatedYaos, "自動搖卦")
                setResult(result)
            }
        }
    }

    fun cancelTaijiLongPress() {
        longPressJob?.cancel()
        _uiState.update { it.copy(isShaking = false, autoShakeProgress = 0f) }
    }

    // 3. 手動搖卦：直接添加一爻
    fun addManualYao(yao: Yao) {
        if (_uiState.value.currentYaos.size >= 6) return
        val updated = _uiState.value.currentYaos + yao
        _uiState.update { it.copy(currentYaos = updated) }

        if (updated.size == 6) {
            val result = engine.buildResultFrom6Yaos(updated, "手動搖卦")
            setResult(result)
        }
    }

    // 4. 類象加方位起卦
    fun performCategoryDirectionDivination(categoryNum: Int, directionNum: Int) {
        val result = engine.calculateCategoryDirectionDivination(categoryNum, directionNum)
        setResult(result)
    }

    fun resetShake() {
        _uiState.update { it.copy(currentYaos = emptyList(), result = null) }
    }

    private fun setResult(result: DivinationResult) {
        _uiState.update {
            it.copy(
                result = result,
                tiYongAnalysis = tiYongAnalyzer.analyze(result),
                historyList = listOf(result) + it.historyList
            )
        }
    }

    fun getAll64Hexagrams(): List<Hexagram> = repository.getAllHexagrams()

    fun getHexagramById(id: Int): Hexagram? = repository.getHexagramById(id)
}
