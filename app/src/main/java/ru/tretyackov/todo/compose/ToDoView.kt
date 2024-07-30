package ru.tretyackov.todo.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.tretyackov.todo.R
import ru.tretyackov.todo.data.ToDoPriority
import ru.tretyackov.todo.data.toFormattedString
import ru.tretyackov.todo.theme.AppTheme
import ru.tretyackov.todo.theme.backgroundColor
import ru.tretyackov.todo.theme.blueColor
import ru.tretyackov.todo.theme.dropDownItemMenuColor
import ru.tretyackov.todo.theme.redColor
import ru.tretyackov.todo.theme.textFieldBackgroundColor
import ru.tretyackov.todo.theme.textFieldTextColor
import ru.tretyackov.todo.ui.IDatePickerDialog
import ru.tretyackov.todo.utilities.DateHelper
import ru.tretyackov.todo.viewmodels.IToDoViewModel
import ru.tretyackov.todo.viewmodels.ToDoUpdateError
import java.util.Date

@Preview(showBackground = true)
@Composable
fun ToDoViewPreview() {
    val mockViewModel = object : IToDoViewModel {
        override val textState: StateFlow<String>
            get() = MutableStateFlow("Текст")
        override val priorityState: StateFlow<ToDoPriority>
            get() = MutableStateFlow(ToDoPriority.Medium)
        override val isDeadlineState: StateFlow<Boolean>
            get() = MutableStateFlow(true)
        override val deadlineDateState: StateFlow<Date>
            get() = MutableStateFlow(DateHelper.now())
        override val deletableState: StateFlow<Boolean>
            get() = MutableStateFlow(true)
        override val isLoadingState: StateFlow<Boolean>
            get() = MutableStateFlow(false)
        override val errorState: StateFlow<ToDoUpdateError>
            get() = MutableStateFlow(ToDoUpdateError.None)

        override fun updateText(text: String) {}
        override fun updatePriority(priority: ToDoPriority) {}
        override fun updateIsDeadline(isDeadline: Boolean) {}
        override fun updateDeadlineDate(date: Date) {}
        override fun deleteToDo() {}
        override fun saveToDo() {}
        override fun goBack() {}
    }
    val mockDatePickerDialog = object : IDatePickerDialog {
        override fun show() {}
        override fun setDate(date: Date) {}
    }
    ToDoView(vm = mockViewModel, datePickerDialog = mockDatePickerDialog)
}

@Composable
fun ToDoView(vm: IToDoViewModel, datePickerDialog: IDatePickerDialog) {
    val text by vm.textState.collectAsState()
    val priority by vm.priorityState.collectAsState()
    val deletable by vm.deletableState.collectAsState()
    val isDeadline by vm.isDeadlineState.collectAsState()
    val deadlineDate by vm.deadlineDateState.collectAsState()
    val isLoading by vm.isLoadingState.collectAsState()
    AppTheme {
        Surface {
            Box {
                if (isLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    ) {
                        CircularProgressIndicator(color = textFieldTextColor)
                        Spacer(modifier = Modifier.padding(16.dp))
                        Text(text = stringResource(id = R.string.loading_text))
                    }
                } else {
                    Scaffold(modifier = Modifier.background(backgroundColor), topBar = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(painter = painterResource(id = R.drawable.close),
                                contentDescription = stringResource(id = R.string.close_content_description),
                                Modifier
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }) {
                                        vm.goBack()
                                    }
                                    .padding(16.dp)
                                    .height(24.dp)
                                    .width(24.dp))
                            TextButton(onClick = vm::saveToDo, modifier = Modifier
                                .testTag("save_button").padding(16.dp)) {
                                Text(stringResource(id = R.string.save), color = blueColor)
                            }
                        }
                    }, containerColor = backgroundColor) { padding ->

                        Column(
                            modifier = Modifier
                                .padding(padding)
                                .verticalScroll(rememberScrollState())
                        ) {
                            TextField(value = text, onValueChange = vm::updateText,
                                Modifier
                                    .testTag("todo_text")
                                    .height(IntrinsicSize.Min)
                                    .padding(start = 16.dp, end = 16.dp)
                                    .fillMaxWidth()
                                    .shadow(elevation = 3.dp), minLines = 5,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = textFieldBackgroundColor,
                                    unfocusedContainerColor = textFieldBackgroundColor,
                                    focusedTextColor = textFieldTextColor,
                                    unfocusedTextColor = textFieldTextColor,
                                ), shape = RoundedCornerShape(8.dp),
                                placeholder = {
                                    Text(
                                        stringResource(id = R.string.to_do_placeholder_text),
                                        Modifier.alpha(0.3f)
                                    )
                                })
                            Column(Modifier.padding(start = 16.dp, end = 16.dp)) {
                                Column(Modifier.padding(top = 16.dp, bottom = 16.dp)) {
                                    Text(
                                        stringResource(id = R.string.priority),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box {
                                        var expanded: Boolean by remember { mutableStateOf(false) }
                                        Text(
                                            text = stringResource(
                                                id = when (priority) {
                                                    ToDoPriority.Low -> R.string.low_priority
                                                    ToDoPriority.Medium -> R.string.medium_priority
                                                    ToDoPriority.High -> R.string.high_priority
                                                }
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.clickable { expanded = true })
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                            modifier = Modifier.background(dropDownItemMenuColor)
                                        ) {
                                            DropdownMenuItem(onClick = {
                                                vm.updatePriority(ToDoPriority.Low)
                                                expanded = false
                                            }, text = {
                                                Text(
                                                    stringResource(id = R.string.low_priority),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                            })
                                            DropdownMenuItem(
                                                onClick = {
                                                    vm.updatePriority(ToDoPriority.Medium)
                                                    expanded = false
                                                },
                                                text = {
                                                    Text(
                                                        stringResource(id = R.string.medium_priority),
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                })
                                            DropdownMenuItem(
                                                onClick = {
                                                    vm.updatePriority(ToDoPriority.High)
                                                    expanded = false
                                                },
                                                text = {
                                                    Text(
                                                        stringResource(id = R.string.high_priority),
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                })
                                        }
                                    }
                                }
                                HorizontalDivider()
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp, bottom = 16.dp)
                                ) {
                                    Column {
                                        Text(
                                            stringResource(id = R.string.deadline),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (isDeadline) {
                                            Text(
                                                deadlineDate.toFormattedString(),
                                                color = blueColor,
                                                modifier = Modifier.clickable {
                                                    datePickerDialog.setDate(deadlineDate)
                                                    datePickerDialog.show()
                                                },
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = isDeadline,
                                        onCheckedChange = { vm.updateIsDeadline(it) })
                                }
                                HorizontalDivider()
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .alpha(if (deletable) 1f else 0.15f)
                                        .background(color = Color.Transparent)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = if (deletable) rememberRipple(color = redColor) else null,
                                            onClick = { if (deletable) vm.deleteToDo() }
                                        )
                                        .padding(0.dp, 16.dp, 16.dp, 16.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.bin),
                                        contentDescription = stringResource(id = R.string.delete_content_description),
                                        Modifier
                                            .height(24.dp)
                                            .width(24.dp),
                                        colorFilter = ColorFilter.tint(color = if (deletable) redColor else textFieldTextColor)
                                    )
                                    Text(
                                        stringResource(id = R.string.delete),
                                        modifier = Modifier.padding(start = 12.dp),
                                        color = if (deletable) redColor else textFieldTextColor,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}