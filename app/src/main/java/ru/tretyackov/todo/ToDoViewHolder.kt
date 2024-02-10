package ru.tretyackov.todo

import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

fun TextView.updateCompleteStyle(isCompleted: Boolean)
{
    setTextColor(Color.parseColor(if(isCompleted) "#B3B3B3" else "#000000"))
    paintFlags = if(isCompleted)
        (paintFlags or Paint.STRIKE_THRU_TEXT_FLAG) else
        (paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv())
}

class ToDoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
    private val textView = itemView.findViewById<TextView>(R.id.toDoNameTextView)
    private val checkBox = itemView.findViewById<CheckBox>(R.id.checkBoxCompleted)
    fun onBind(todo:ToDo){
        textView.text = todo.name
        checkBox.isChecked = todo.completed
        textView.updateCompleteStyle(todo.completed)
        checkBox.setOnClickListener{
            todo.completed = !todo.completed
            textView.updateCompleteStyle(todo.completed)
        }
    }
}