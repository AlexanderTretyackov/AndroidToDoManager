package ru.tretyackov.todo.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import ru.tretyackov.todo.R

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val customTypography = Typography(
        titleLarge = TextStyle(fontSize = 32.sp),
        titleMedium = TextStyle(fontSize = 20.sp),
        bodyMedium = TextStyle(fontSize = 16.sp),
        bodySmall = TextStyle(fontSize = 14.sp),
    )
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) {
            darkColorScheme(
                primary = primaryColor,
                onPrimary = blueColor,
                surface = Color(0xFF161618),
                onSurface = Color.White,
            )
        } else lightColorScheme(
            primary = blueColor,
            onPrimary = blueColor,
            surface = Color(0xFFF7F6F2),
            onSurface = Color.Black,
        ), content = {
            ProvideTextStyle(
                value = TextStyle(color = colorResource(id = R.color.defaultTextColor)),
                content = content
            )
        },
        typography = customTypography
    )
}
