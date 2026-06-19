package com.lnxteam.atmoscope.ui.screens

import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lnxteam.atmoscope.ui.theme.PurpleLight
import com.lnxteam.atmoscope.ui.theme.Teal
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.lnxteam.atmoscope.R

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                1f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        alpha.animateTo(1f, animationSpec = tween(600))
        delay(300)
        subtitleAlpha.animateTo(1f, animationSpec = tween(500))
        delay(1500)

        alpha.animateTo(0f, animationSpec = tween(400))
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0E1A), Color(0xFF0D1117))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        SplashStarfield()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.atmoscope_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Atmoscope",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PurpleLight, Teal)
                    )
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Weather & Astronomy",
                fontSize = 14.sp,
                color = Color(0xFF8B949E),
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }
    }
}

@Composable
fun SplashStarfield() {
    val stars = remember {
        List(80) {
            Triple(
                (0..100).random() / 100f,
                (0..100).random() / 100f,
                (2..6).random().dp
            )
        }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        stars.forEachIndexed { i, (x, y, size) ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)
            ) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = (x * 400).dp,
                            y = (y * 800).dp
                        )
                        .size(size)
                        .alpha(if (i % 3 == 0) twinkle else 1f - twinkle * 0.5f)
                        .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    }
}