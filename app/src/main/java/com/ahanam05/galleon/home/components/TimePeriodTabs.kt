package com.ahanam05.galleon.home.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahanam05.galleon.home.Modes
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun TimePeriodTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf(Modes.DAILY, Modes.WEEKLY, Modes.MONTHLY)
    val density = LocalDensity.current

    var tabSize by remember { mutableStateOf(IntSize.Zero) }
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    val targetOffset = with(density) {
        (tabSize.width * selectedIndex).toDp()
    }

    val animatedOffset: Dp by animateDpAsState(
        targetValue = targetOffset,
        label = "tabOffsetAnimation"
    )

    val tabShape = RoundedCornerShape(22.dp)
    val containerColor = Color.White

    Surface(
        color = containerColor,
        shadowElevation = 6.dp,
        shape = tabShape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .height(44.dp)
            .onSizeChanged { newSize ->
                tabSize = IntSize(width = newSize.width / tabs.size, height = newSize.height)
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .width(with(density) { tabSize.width.toDp() })
                    .fillMaxHeight()
                    .padding(4.dp)
                    .background(
                        color = MutedGold,
                        shape = tabShape
                    )
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                tabs.forEach { tab ->
                    val selected = tab == selectedTab

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                indication = null,
                                interactionSource = remember {
                                    MutableInteractionSource()
                                }
                            ) { onTabSelected(tab) }
                    ) {
                        Text(
                            text = tab,
                            color = if (selected) Color.White else Color(0xFF777777),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}
