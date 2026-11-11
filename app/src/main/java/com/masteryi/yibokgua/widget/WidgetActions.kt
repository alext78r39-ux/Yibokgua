package com.masteryi.yibokgua.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import java.time.LocalDate
import kotlin.random.Random

/** 按下「換一卦」：隨機挑一卦並覆寫當日顯示，然後更新所有小工具。 */
class RefreshWidgetAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val today = LocalDate.now().toEpochDay()
        val repository = WidgetStateRepository(context)
        val current = WidgetHexagram.resolveId(repository.state(), today)

        var nextId = Random.nextInt(1, WidgetHexagram.HEXAGRAM_COUNT + 1)
        if (nextId == current) {
            nextId = nextId % WidgetHexagram.HEXAGRAM_COUNT + 1
        }
        repository.setOverride(nextId, today)
        HexagramWidget().updateAll(context)
    }
}
