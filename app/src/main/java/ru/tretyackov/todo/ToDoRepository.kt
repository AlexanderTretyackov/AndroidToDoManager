package ru.tretyackov.todo

import androidx.lifecycle.MutableLiveData

object ToDoRepository
{
    val todos : MutableLiveData<MutableList<ToDo>> = MutableLiveData<MutableList<ToDo>>(
        mutableListOf(ToDo("Название", false),
            ToDo("0", false),
            ToDo("1", false),
            ToDo("2", false),
            ToDo("3", false),
            ToDo("4", false),
            ToDo("5", false),
            ToDo("6", false),
            ToDo("7", false),
            ToDo("8", false),
            ToDo("9", false),
            ToDo("10", false),
            ToDo("11", false),
            ToDo("0", false),
            ToDo("1", false),
            ToDo("2", false),
            ToDo("3", false),
            ToDo("4", false),
            ToDo("5", false),
            ToDo("6", false),
            ToDo("7", false),
            ToDo("8", false),
            ToDo("9", false),
            ToDo("10", false),
            ToDo("11", false),
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
        oldToDo.priority = newToDo.priority
        oldToDo.deadline = newToDo.deadline
        todos.value = todos.value
    }
}

fun ToDoRepository.find(id:String):ToDo?{
    return ToDoRepository.todos.value?.find { toDo -> toDo.id == id }
}