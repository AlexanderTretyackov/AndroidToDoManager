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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.tretyackov.todo.R
import ru.tretyackov.todo.compose.ToDoView
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.utilities.DateHelper
import ru.tretyackov.todo.utilities.getAppComponent
import ru.tretyackov.todo.viewmodels.ToDoUpdateError
import ru.tretyackov.todo.viewmodels.ToDoViewModel
import ru.tretyackov.todo.viewmodels.lazyViewModel
import java.util.Calendar
import java.util.Date

interface IDatePickerDialog{
    fun show()
    fun setDate(date:Date)
}

class ToDoFragment(private val todoItemParam: TodoItem? = null) : Fragment() {
    val vm: ToDoViewModel by lazyViewModel { stateHandle ->
        getAppComponent().toDoViewModelFactory().create(stateHandle)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(todoItemParam != null)
            vm.updateTodoItem(todoItemParam)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.goBackState.collect { if(it) parentFragmentManager.popBackStack() }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.errorState.collect {
                    val errorText = when(it){
                        ToDoUpdateError.None -> null
                        ToDoUpdateError.EmptyText -> context?.getString(R.string.error_empty_text_todo)
                        ToDoUpdateError.Network -> context?.getString(R.string.network_error)
                    }
                    if(errorText != null)
                        Toast.makeText(this@ToDoFragment.requireContext(),
                            errorText, Toast.LENGTH_SHORT)
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

