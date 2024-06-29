package ru.tretyackov.todo.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

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