package ru.tretyackov.todo.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.tretyackov.todo.R
import ru.tretyackov.todo.compose.ToDoView
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.utilities.DateHelper
import ru.tretyackov.todo.viewmodels.ToDoViewModel
import java.util.Calendar
import java.util.Date

interface IDatePickerDialog{
    fun show()
    fun setDate(date:Date)
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
                vm.goBackState.collect { if(it) parentFragmentManager.popBackStack() }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.showTextErrorState.collect {
                    if(it) Toast.makeText(this@ToDoFragment.requireContext(),
                        R.string.error_empty_text_todo, Toast.LENGTH_SHORT)
                        .show() }
            }
        }
        val datePickerDialogImpl = object : IDatePickerDialog {
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

