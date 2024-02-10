package ru.tretyackov.todo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ToDoAdapter : RecyclerView.Adapter<ToDoViewHolder>() {
    var todos = listOf<ToDo>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ToDoViewHolder(layoutInflater.inflate(R.layout.to_do_list_item, parent, false))
    }

    override fun getItemCount(): Int = todos.size

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.onBind(todos[position])
    }
}