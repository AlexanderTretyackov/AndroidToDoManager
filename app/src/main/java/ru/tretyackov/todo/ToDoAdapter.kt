package ru.tretyackov.todo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.tretyackov.todo.databinding.ToDoListItemBinding

class ToDoAdapter(private val onClick:(ToDo)->Unit,
                  private val onCheck : (ToDo)->Unit) : RecyclerView.Adapter<ToDoViewHolder>() {
    var todos = listOf<ToDo>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ToDoViewHolder(ToDoListItemBinding.inflate(layoutInflater, parent, false),
            onClick, onCheck)
    }

    override fun getItemCount(): Int = todos.size

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.onBind(todos[position])
    }
}