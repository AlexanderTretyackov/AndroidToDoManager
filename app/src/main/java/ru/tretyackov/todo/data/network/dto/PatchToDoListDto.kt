package ru.tretyackov.todo.data.network.dto

data class PatchToDoListDto(val list : List<ToDoItemDto>, val revision : Int)