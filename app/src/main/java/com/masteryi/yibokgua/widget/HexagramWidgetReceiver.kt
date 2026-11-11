package com.masteryi.yibokgua.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** 「易卜卦」桌面小工具的 AppWidgetProvider。 */
class HexagramWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HexagramWidget()
}
