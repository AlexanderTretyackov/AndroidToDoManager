package ru.tretyackov.todo.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import ru.tretyackov.todo.R

val blueColor = Color(0xFF007AFF)

val primaryColor = Color(0x4D007AFF)
val whiteColor = Color(0xFFFFFFFF)
val blackSecondColor = Color(0xFF252528)
val checkedColor = Color(0xFF34C759)
val uncheckedColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0x33FFFFFF) else Color(0x33000000)
val highPriorityUncheckedColor = Color(0xFFFF453A)
val highPriorityUncheckedBoxColor = Color(0x33FF453A)

val redColor = Color(0xFFFF3B30)

val backgroundColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF161618) else Color(0xFFF7F6F2)

val accentBackgroundColor: Color
    @Composable
    get() = colorResource(R.color.accentBackgroundColor)

val textFieldBackgroundColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) blackSecondColor else whiteColor

val textFieldTextColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color.White else Color.Black

val dropDownItemMenuColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF3C3C3F) else Color.White

val iconsColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color.White else Color.Black