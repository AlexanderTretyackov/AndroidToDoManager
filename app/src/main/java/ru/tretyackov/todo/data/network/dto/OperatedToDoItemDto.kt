package ru.tretyackov.todo.data.network.dto

data class OperatedToDoItemDto(val revision: Int, val status: String, val element: ToDoItemDto)
