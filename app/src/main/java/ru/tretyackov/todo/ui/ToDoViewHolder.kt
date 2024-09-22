package ru.tretyackov.todo.ui

import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.tretyackov.todo.R
import ru.tretyackov.todo.data.ToDoPriority
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.data.toFormattedString
import ru.tretyackov.todo.databinding.ToDoListItemBinding

fun TextView.updateCompleteStyle(isCompleted: Boolean)
{
    setTextColor(if(isCompleted) Color.parseColor("#B3B3B3") else
        ContextCompat.getColor(context, R.color.defaultTextColor))
    paintFlags = if(isCompleted)
        (paintFlags or Paint.STRIKE_THRU_TEXT_FLAG) else
        (paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv())
}

class ToDoViewHolder(binding: ToDoListItemBinding, private val onClick : (TodoItem)->Unit,
                     private val onCheck : (TodoItem)->Unit) : RecyclerView.ViewHolder(binding.root)
{
    private var _todoItem : TodoItem? = null
    val toDoItem : TodoItem? get() = _todoItem
    private val textView = binding.toDoNameTextView
    private val checkBox = binding.checkBoxCompleted
    private val priorityImageView = binding.priorityImageView
    private val itemDeadlineTextView = binding.itemDeadlineTextView
    fun onBind(todo: TodoItem){
        _todoItem=todo
        textView.text = todo.name
        checkBox.isChecked = todo.completed
        textView.updateCompleteStyle(todo.completed)
        priorityImageView.visibility = View.VISIBLE
        when(todo.priority){
            ToDoPriority.Low -> priorityImageView.setImageResource(R.drawable.low)
            ToDoPriority.Medium -> priorityImageView.visibility = View.GONE
            ToDoPriority.High -> priorityImageView.setImageResource(R.drawable.high)
        }
        itemDeadlineTextView.visibility = if(todo.deadline != null) View.VISIBLE else View.GONE
        itemDeadlineTextView.text = todo.deadline?.toFormattedString()
        checkBox.setOnClickListener{
            onCheck(todo)
        }
        itemView.setOnClickListener {
            onClick(todo)
        }
    }
}