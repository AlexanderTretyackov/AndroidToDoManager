package ru.tretyackov.todo

import androidx.lifecycle.MutableLiveData

object ToDoRepository
{
    val todos : MutableLiveData<MutableList<TodoItem>> = MutableLiveData<MutableList<TodoItem>>(
        mutableListOf(TodoItem("Название", false),
            TodoItem("0", false),
            TodoItem("1", false),
            TodoItem("2", false),
            TodoItem("3", false),
            TodoItem("4", false),
            TodoItem("5", false),
            TodoItem("6", false),
            TodoItem("7", false),
            TodoItem("8", false),
            TodoItem("9", false),
            TodoItem("10", false),
            TodoItem("11", false),
            TodoItem("0", false),
            TodoItem("1", false),
            TodoItem("2", false),
            TodoItem("3", false),
            TodoItem("4", false),
            TodoItem("5", false),
            TodoItem("6", false),
            TodoItem("7", false),
            TodoItem("8", false),
            TodoItem("9", false),
            TodoItem("10", false),
            TodoItem("11", false),
        TodoItem("Название 2 Название 2Название 2Название 2Название 2Название 2Название 2", true))
    )

    fun add(todoItem: TodoItem) {
        todos.value!!.add(todoItem)
        todos.value = todos.value
    }

    fun remove(todoItem: TodoItem) {
        todos.value!!.remove(todoItem)
        todos.value = todos.value
    }

    fun update(oldTodoItem: TodoItem, newTodoItem: TodoItem) {
        oldTodoItem.name = newTodoItem.name
        oldTodoItem.priority = newTodoItem.priority
        oldTodoItem.deadline = newTodoItem.deadline
        todos.value = todos.value
    }
}

fun ToDoRepository.find(id:String):TodoItem?{
    return ToDoRepository.todos.value?.find { toDo -> toDo.id == id }
}