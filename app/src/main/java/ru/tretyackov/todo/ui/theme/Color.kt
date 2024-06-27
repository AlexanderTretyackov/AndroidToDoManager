package ru.tretyackov.todo.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val blueColor = Color(0xFF007AFF)

val whiteColor = Color(0xFFFFFFFF)
val blackColor = Color(0xFF000000)
val blackSecondColor = Color(0xFF252528)

val redColor = Color(0xFFFF3B30)

val backgroundColor: Color @Composable
get() = if (isSystemInDarkTheme()) Color(0xFF161618) else Color(0xFFF7F6F2)

val textFieldBackgroundColor: Color @Composable
get() = if (isSystemInDarkTheme()) blackSecondColor else whiteColor

val textFieldTextColor: Color @Composable
get() = if (isSystemInDarkTheme()) Color.White else Color.Black

val dropDownItemMenuColor: Color @Composable
get() = if (isSystemInDarkTheme()) Color(0xFF3C3C3F) else Color.White