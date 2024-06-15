package ru.tretyackov.todo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.tretyackov.todo.databinding.ToDoListItemBinding

class ToDoAdapter(private val onClick:(TodoItem)->Unit,
                  private val onCheck : (TodoItem)->Unit) : RecyclerView.Adapter<ToDoViewHolder>() {
    var todos = listOf<TodoItem>()
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