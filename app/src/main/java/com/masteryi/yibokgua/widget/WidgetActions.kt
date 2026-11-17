package com.masteryi.yibokgua.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll

/** 按下「重新整理」：依當下農曆日期與時辰重新起卦並更新所有小工具。 */
class RefreshWidgetAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        HexagramWidget().updateAll(context)
    }
}
