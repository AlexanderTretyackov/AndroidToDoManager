package ru.tretyackov.todo

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.tretyackov.todo.databinding.FragmentToDoBinding
import java.util.Calendar
import java.util.Date


private const val TODO_ID = "TODO_ID"
private const val DEADLINE_DATE = "DEADLINE_DATE"

class ToDoFragment(private var toDoParam: ToDo? = null) : Fragment(), AdapterView.OnItemSelectedListener {

    private var deadline = Calendar.getInstance().time
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        toDoParam = initToDo(savedInstanceState)
        val toDo = toDoParam

        val binding = FragmentToDoBinding.inflate(layoutInflater, container, false)

        val editText = binding.toDoDescriptionEditText
        val closeImageButton = binding.closeImageButton
        val saveButton = binding.saveButton
        val deadlineTextView = binding.deadlineTextView
        val switchCompat = binding.switchCompat
        val deleteTextView = binding.deleteTextView
        val deleteImageView = binding.deleteImageView

        closeImageButton.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        editText.setText(toDo?.name ?: "")

        if(toDo != null)
        {
            val color = ContextCompat.getColor(requireContext(), R.color.deleteColor)
            deleteTextView.setTextColor(color)
            deleteImageView.setColorFilter(color)
            val deleteClickListener = OnClickListener {
                ToDoRepository.remove(toDo)
                parentFragmentManager.popBackStack()
            }
            deleteTextView.setOnClickListener(deleteClickListener)
            deleteImageView.setOnClickListener(deleteClickListener)
        }

        val spinner = binding.spinner
        if(savedInstanceState == null && toDo != null)
        {
            spinner.setSelection(toDo.priority.ordinal,false)
        }
        spinner.onItemSelectedListener = this

        val datePickerDialog = buildDatePickerDialog{ _: DatePicker, y: Int, m: Int, d: Int ->
            deadline = dateFromYearMonthDay(y,m,d)
            deadlineTextView.text = deadline.toFormattedString() }
        if(toDo?.deadline != null)
        {
            deadline = toDo.deadline!!
            switchCompat.isChecked = true
            deadlineTextView.visibility = View.VISIBLE
        }
        val savedDate = savedInstanceState?.getSerializable(DEADLINE_DATE) as Date?
        if(savedDate != null)
            deadline = savedDate
        deadlineTextView.text = deadline.toFormattedString()
        deadlineTextView.setOnClickListener{
            datePickerDialog.show()
        }
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                deadlineTextView.visibility = View.VISIBLE
            } else
            {
                deadlineTextView.visibility = View.INVISIBLE
            }
        }

        saveButton.setOnClickListener{
            if(toDo != null)
                ToDoRepository.update(toDo, ToDo(editText.text.toString().trim(),
                    toDo.completed, toDo.id, toDo.createdAt,
                    priority = ToDoPriority.values()[spinner.selectedItemPosition],
                    deadline = if(switchCompat.isChecked) this.deadline else null))
            else
                ToDoRepository.add(ToDo(editText.text.toString().trim(), false,
                    priority = ToDoPriority.values()[spinner.selectedItemPosition],
                    deadline = if(switchCompat.isChecked) this.deadline else null))
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun dateFromYearMonthDay(year:Int,month:Int,day:Int): Date
    {
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(year,month,day)
        return calendar.time
    }

    private fun buildDatePickerDialog(dateListener: DatePickerDialog.OnDateSetListener):DatePickerDialog{
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH)
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(requireContext(),
            dateListener ,year,month,day)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        return datePickerDialog
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {}

    override fun onNothingSelected(parent: AdapterView<*>) { }

    private fun initToDo(savedInstanceState: Bundle?) : ToDo?
    {
        if(toDoParam!=null)
            return toDoParam
        val id = savedInstanceState?.getString(TODO_ID, null)
        if(id != null)
            return ToDoRepository.find(id)
        return null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(toDoParam!=null)
        {
            outState.putString(TODO_ID, toDoParam!!.id)
            outState.putSerializable(DEADLINE_DATE, deadline)
        }
    }
}