package com.masteryi.yibokgua.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** 「九宮六壬」桌面小工具的 AppWidgetProvider。 */
class NinePalaceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NinePalaceWidget()
}
