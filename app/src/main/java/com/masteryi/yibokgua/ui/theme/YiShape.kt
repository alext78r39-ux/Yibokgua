package com.masteryi.yibokgua.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * 圓角 Tokens。
 */
object YiShape {
    val tinyRadius = 4.dp
    val smallRadius = 8.dp
    val mediumRadius = 16.dp
    val largeRadius = 24.dp
    val pillRadius = 999.dp

    val tiny = RoundedCornerShape(tinyRadius)
    val small = RoundedCornerShape(smallRadius)
    val medium = RoundedCornerShape(mediumRadius)
    val large = RoundedCornerShape(largeRadius)
    val pill = RoundedCornerShape(pillRadius)
}
