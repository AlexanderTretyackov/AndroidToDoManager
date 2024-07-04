package ru.tretyackov.todo.data

data class OperatedToDoItemDto(val revision : Int, val status : String, val element: ToDoItemDto)