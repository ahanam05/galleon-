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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahanam05.galleon.R.*

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
            painter = painterResource(id = drawable.galleon_logo),
            contentDescription = stringResource(id = string.galleon_logo_desc),
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
            text = stringResource(id = string.app_name),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D2D2D),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = stringResource(id = string.landing_screen_tagline),
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
                    painter = painterResource(id = drawable.siwg_button),
                    contentDescription = stringResource(id = string.sign_in_button_desc),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = string.sign_in_text),
            fontSize = 13.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center
        )
    }
}
