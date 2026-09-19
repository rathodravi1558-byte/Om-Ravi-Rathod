package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.model.ConditionType
import com.example.ui.theme.WeatherGradients

@Composable
fun AtmosphericBackground(
    conditionType: ConditionType,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val gradientColors = WeatherGradients.getGradient(conditionType)
    
    val color1 by animateColorAsState(targetValue = gradientColors[0], animationSpec = tween(800), label = "grad1")
    val color2 by animateColorAsState(targetValue = gradientColors[1], animationSpec = tween(800), label = "grad2")
    val color3 by animateColorAsState(targetValue = gradientColors[2], animationSpec = tween(800), label = "grad3")

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(color1, color2, color3)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        content()
    }
}
