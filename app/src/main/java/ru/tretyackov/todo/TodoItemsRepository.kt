package ru.tretyackov.todo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

object TodoItemsRepository
{
    private val context = newSingleThreadContext("CounterContext")
    private val todos = MutableStateFlow(mutableListOf(TodoItem("Название", false),
            TodoItem("0", false),
            TodoItem("1", false, priority = ToDoPriority.Low),
            TodoItem("2", false, priority = ToDoPriority.High),
            TodoItem("3", true),
            TodoItem("4", true, priority = ToDoPriority.Low),
            TodoItem("5", true, priority = ToDoPriority.High),
            TodoItem("Длинный текст длинный текст длинный текст длинный текст длинный текст длинный текст длинный текст длинный текст",
                false),
            TodoItem("Длинный текст длинный текст длинный текст длинный текст длинный текст длинный текст длинный текст длинный текст",
                false, deadline = DateHelper.dateFromYearMonthDay(2024,8,29)),
            TodoItem("7", false, deadline = DateHelper.dateFromYearMonthDay(2024,8,29)),
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

    fun getAll() : StateFlow<List<TodoItem>> = todos

    suspend fun add(todoItem: TodoItem) {
        withContext(context){
            val newList = todos.value.toMutableList()
            newList.add(todoItem)
            todos.update { newList }
        }
    }

    suspend fun remove(todoItem: TodoItem) {
        withContext(context){
            val newList = todos.value.toMutableList()
            newList.remove(todoItem)
            todos.update { newList }
        }
    }

    suspend fun update(oldTodoItem: TodoItem, newTodoItem: TodoItem) {
        withContext(context){
            val newList = todos.value.toMutableList()
            val indexOldTodoItem = newList.indexOf(oldTodoItem)
            newList[indexOldTodoItem] = newTodoItem
            todos.update { newList }
        }
    }

    suspend fun find(id:String):TodoItem?{
        return withContext(context) {
           return@withContext todos.value.find { toDo -> toDo.id == id }
        }
    }
}
