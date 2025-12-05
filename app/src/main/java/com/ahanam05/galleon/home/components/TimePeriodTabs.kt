package com.ahanam05.galleon.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahanam05.galleon.home.Modes


@Composable
fun TimePeriodTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf(Modes.DAILY, Modes.WEEKLY, Modes.MONTHLY)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab

            Surface(
                color = if (selected) Color(0xFFE0B663) else Color.White,
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .height(40.dp)
                    .weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onTabSelected(tab) }
                ) {
                    Text(
                        text = tab,
                        color = if (selected) Color.Black else Color(0xFF777777),
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}