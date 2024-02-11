package ru.tretyackov.todo

import androidx.lifecycle.MutableLiveData

object ToDoRepository
{
    val todos : MutableLiveData<MutableList<ToDo>> = MutableLiveData<MutableList<ToDo>>(
        mutableListOf(ToDo("Название", false),
        ToDo("Название 2 Название 2Название 2Название 2Название 2Название 2Название 2", true))
    )

    fun add(toDo: ToDo) {
        todos.value!!.add(toDo)
        todos.value = todos.value
    }

    fun remove(toDo: ToDo) {
        todos.value!!.remove(toDo)
        todos.value = todos.value
    }

    fun update(oldToDo: ToDo, newToDo: ToDo) {
        oldToDo.name = newToDo.name
        todos.value = todos.value
    }
}

fun ToDoRepository.find(id:String):ToDo?{
    return ToDoRepository.todos.value?.find { toDo -> toDo.id == id }
}