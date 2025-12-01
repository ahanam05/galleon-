package com.ahanam05.galleon

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LandingScreen(onSignInClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.galleon_logo),
            contentDescription = "Galleon Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(160.dp)
                .padding(bottom = 32.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(80.dp),
                    spotColor = Color(0xFFFFB74D).copy(alpha = 0.3f)
                )
        )

        Text(
            text = "Galleon",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D2D2D),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Expense tracking\nmade magical",
            fontSize = 18.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        Card(
            modifier = Modifier
                .height(56.dp)
                .widthIn(min = 240.dp, max = 320.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clickable(enabled = true, onClick = onSignInClick),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.siwg_button),
                    contentDescription = "Sign in with Google button",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Already have account? Sign in with Google",
            fontSize = 13.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center
        )
    }
}