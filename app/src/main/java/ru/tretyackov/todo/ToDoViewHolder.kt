package ru.tretyackov.todo

import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

fun TextView.updateCompleteStyle(isCompleted: Boolean)
{
    setTextColor(Color.parseColor(if(isCompleted) "#B3B3B3" else "#000000"))
    paintFlags = if(isCompleted)
        (paintFlags or Paint.STRIKE_THRU_TEXT_FLAG) else
        (paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv())
}

class ToDoViewHolder(itemView: View, private val onClick : (ToDo)->Unit,
                     private val onCheck : (ToDo)->Unit) : RecyclerView.ViewHolder(itemView)
{
    private val textView = itemView.findViewById<TextView>(R.id.toDoNameTextView)
    private val checkBox = itemView.findViewById<CheckBox>(R.id.checkBoxCompleted)
    private val priorityImageView = itemView.findViewById<ImageView>(R.id.priorityImageView)
    private val itemDeadlineTextView = itemView.findViewById<TextView>(R.id.itemDeadlineTextView)
    fun onBind(todo:ToDo){
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