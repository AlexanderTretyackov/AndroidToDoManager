package ru.tretyackov.todo

import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.tretyackov.todo.databinding.ToDoListItemBinding

fun TextView.updateCompleteStyle(isCompleted: Boolean)
{
    setTextColor(if(isCompleted) Color.parseColor("#B3B3B3") else
        ContextCompat.getColor(context,R.color.defaultTextColor))
    paintFlags = if(isCompleted)
        (paintFlags or Paint.STRIKE_THRU_TEXT_FLAG) else
        (paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv())
}

class ToDoViewHolder(binding: ToDoListItemBinding, private val onClick : (TodoItem)->Unit,
                     private val onCheck : (TodoItem)->Unit) : RecyclerView.ViewHolder(binding.root)
{
    private val textView = binding.toDoNameTextView
    private val checkBox = binding.checkBoxCompleted
    private val priorityImageView = binding.priorityImageView
    private val itemDeadlineTextView = binding.itemDeadlineTextView
    fun onBind(todo:TodoItem){
        textView.text = todo.name
        checkBox.isChecked = todo.completed
        textView.updateCompleteStyle(todo.completed)
        priorityImageView.visibility = View.VISIBLE
        when(todo.priority){
            ToDoPriority.No ->  priorityImageView.visibility = View.GONE
            ToDoPriority.Low ->  priorityImageView.setImageResource(R.drawable.low)
            ToDoPriority.High ->  priorityImageView.setImageResource(R.drawable.high)
        }
        itemDeadlineTextView.visibility = if(todo.deadline != null) View.VISIBLE else View.GONE
        itemDeadlineTextView.text = todo.deadline?.toFormattedString()
        checkBox.setOnClickListener{
            todo.completed = !todo.completed
            textView.updateCompleteStyle(todo.completed)
            onCheck(todo)
        }
        itemView.setOnClickListener {
            onClick(todo)
        }
    }
}