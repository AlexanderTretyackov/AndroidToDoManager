package ru.tretyackov.todo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val textColor = if(isSystemInDarkTheme()) whiteColor else blackColor
    MaterialTheme(colorScheme = if(isSystemInDarkTheme()){
        darkColorScheme(primary = blueColor)
    } else lightColorScheme(primary = blueColor), content = {
        ProvideTextStyle(
            value = TextStyle(color = textColor),
            content = content
        )
    })
}