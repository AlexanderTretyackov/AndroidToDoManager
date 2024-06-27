package ru.tretyackov.todo

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.tretyackov.todo.ui.theme.AppTheme
import ru.tretyackov.todo.ui.theme.backgroundColor
import ru.tretyackov.todo.ui.theme.blueColor
import ru.tretyackov.todo.ui.theme.dropDownItemMenuColor
import ru.tretyackov.todo.ui.theme.redColor
import ru.tretyackov.todo.ui.theme.textFieldBackgroundColor
import ru.tretyackov.todo.ui.theme.textFieldTextColor
import java.util.Calendar
import java.util.Date

@Composable
fun SettingHeaderStyle(content: @Composable () -> Unit) {
    ProvideTextStyle(value = TextStyle(fontSize = 16.sp)) {
        content()
    }
}

@Composable
fun SettingTextStyle(content: @Composable () -> Unit) {
    ProvideTextStyle(value = TextStyle(fontSize = 14.sp)) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun ToDoViewPreview(){
    val mockViewModel = object : IToDoViewModel{
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
        override fun updateText(text: String) { }
        override fun updatePriority(priority: ToDoPriority) { }
        override fun updateIsDeadline(isDeadline: Boolean) { }
        override fun updateDeadlineDate(date: Date) {  }
        override fun deleteToDo() { }
        override fun saveToDo() {  }
        override fun goBack() { }
    }
    val mockDatePickerDialog = object : IDatePickerDialog{
        override fun show() { }
        override fun setDate(date: Date) { }
    }
    ToDoView(vm = mockViewModel, datePickerDialog = mockDatePickerDialog)
}

interface IToDoViewModel{
    val textState: StateFlow<String>
    val priorityState: StateFlow<ToDoPriority>
    val isDeadlineState: StateFlow<Boolean>
    val deadlineDateState: StateFlow<Date>
    val deletableState: StateFlow<Boolean>
    fun updateText(text: String)
    fun updatePriority(priority: ToDoPriority)
    fun updateIsDeadline(isDeadline:Boolean)
    fun updateDeadlineDate(date: Date)
    fun deleteToDo()
    fun saveToDo()
    fun goBack()
}

interface IDatePickerDialog{
    fun show()
    fun setDate(date:Date)
}

@Composable
fun ToDoView(vm:IToDoViewModel, datePickerDialog : IDatePickerDialog) {
    val text by vm.textState.collectAsState()
    val priority by vm.priorityState.collectAsState()
    val deletable by vm.deletableState.collectAsState()
    val isDeadline by vm.isDeadlineState.collectAsState()
    val deadlineDate by vm.deadlineDateState.collectAsState()
    AppTheme{
        Scaffold (modifier = Modifier.background(backgroundColor), topBar = {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()){
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
                TextButton(onClick = vm::saveToDo, modifier = Modifier.padding(16.dp)){
                    Text(stringResource(id = R.string.save), color = blueColor)
                }
            }
        }, containerColor = backgroundColor) { padding ->
            Column(modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())){
                TextField (value = text, onValueChange = vm::updateText,
                    Modifier
                        .height(IntrinsicSize.Min)
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .shadow(elevation = 3.dp), minLines = 5,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = textFieldBackgroundColor,
                        unfocusedContainerColor = textFieldBackgroundColor,
                        focusedTextColor = textFieldTextColor,
                        unfocusedTextColor = textFieldTextColor,
                    ), shape = RoundedCornerShape(8.dp),
                    placeholder = {Text(stringResource(id = R.string.to_do_placeholder_text), Modifier.alpha(0.3f))})
                Column(Modifier.padding(start = 16.dp, end = 16.dp)){
                    Column(Modifier.padding(top = 16.dp, bottom = 16.dp)){
                        SettingHeaderStyle{
                            Text(stringResource(id = R.string.priority), fontSize = 16.sp)
                        }
                        Box {
                            var expanded: Boolean by remember { mutableStateOf(false) }
                            SettingTextStyle {
                                Text(
                                    text = stringResource(id = when (priority) {
                                        ToDoPriority.Low -> R.string.low_priority
                                        ToDoPriority.Medium -> R.string.medium_priority
                                        ToDoPriority.High -> R.string.high_priority
                                    }),
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable { expanded = true })
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false},
                                modifier = Modifier.background(dropDownItemMenuColor)
                            ) {
                                DropdownMenuItem(onClick = { vm.updatePriority(ToDoPriority.Low)
                                    expanded = false }, text = { Text(stringResource(id = R.string.low_priority))})
                                DropdownMenuItem(onClick = { vm.updatePriority(ToDoPriority.Medium)
                                    expanded = false }, text = {Text(stringResource(id = R.string.medium_priority))})
                                DropdownMenuItem(onClick = { vm.updatePriority(ToDoPriority.High)
                                    expanded = false }, text = {Text(stringResource(id = R.string.high_priority))})
                            }
                        }
                    }
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp)){
                        Column {
                            SettingHeaderStyle{
                                Text(stringResource(id = R.string.deadline))
                            }
                            if(isDeadline)
                            {
                                SettingTextStyle {
                                    Text(deadlineDate.toFormattedString(), color = blueColor,
                                        modifier = Modifier.clickable {
                                            datePickerDialog.setDate(deadlineDate)
                                            datePickerDialog.show() })
                                }
                            }
                        }
                        Switch(checked = isDeadline, onCheckedChange = { vm.updateIsDeadline(it) })
                    }
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .alpha(if (deletable) 1f else 0.15f)
                            .clickable(indication = null,
                                interactionSource = remember { MutableInteractionSource() })
                            { if (deletable) vm.deleteToDo() }
                            .padding(top = 16.dp, bottom = 16.dp)){
                        Image(painter = painterResource(id = R.drawable.bin),
                            contentDescription = stringResource(id = R.string.delete_content_description),
                            Modifier
                                .height(24.dp)
                                .width(24.dp), colorFilter = ColorFilter.tint(color = if(deletable) redColor else textFieldTextColor))
                        Text(stringResource(id = R.string.delete),
                            modifier = Modifier.padding(start = 12.dp), color = if(deletable) redColor else textFieldTextColor)
                    }
                }
            }
        }
    }
}

class ToDoFragment(private val todoItemParam: TodoItem? = null) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vm: ToDoViewModel by viewModels()
        if(todoItemParam != null)
            vm.updateTodoItem(todoItemParam)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.goBackState.collect {
                    if(it) parentFragmentManager.popBackStack() }
            }
        }
        val datePickerDialogImpl = object : IDatePickerDialog{
            private val datePickerDialog = buildDatePickerDialog { _: DatePicker, y: Int, m: Int, d: Int ->
                vm.updateDeadlineDate(DateHelper.dateFromYearMonthDay(y,m,d)) }
            override fun show() {
                datePickerDialog.show()
            }
            override fun setDate(date: Date) {
                datePickerDialog.updateDate(date)
            }
        }
        return ComposeView(requireContext()).apply {
            setContent {
                ToDoView(vm,datePickerDialogImpl)
            }
        }
    }

    private fun DatePickerDialog.updateDate(date : Date){
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = date
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        this.updateDate(year, month, day)
    }

    private fun buildDatePickerDialog(dateListener: DatePickerDialog.OnDateSetListener):DatePickerDialog{
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(requireContext(),dateListener,year,month,day)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        return datePickerDialog
    }
}

