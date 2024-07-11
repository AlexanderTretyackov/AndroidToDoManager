package ru.tretyackov.todo.data.network.dto

data class ToDoListDto(val status:String, val list: List<ToDoItemDto>, val revision: Int)