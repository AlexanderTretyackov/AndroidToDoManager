package ru.tretyackov.todo

import java.util.Calendar

object ToDoRepository
{
    var todos = listOf(ToDo("1", "Название", false,
        Calendar.getInstance().time),
        ToDo("2", "Название 2 Название 2Название 2Название 2Название 2Название 2Название 2", true,
            Calendar.getInstance().time))
}