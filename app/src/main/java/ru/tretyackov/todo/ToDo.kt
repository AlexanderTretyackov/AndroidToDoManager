package ru.tretyackov.todo

import java.util.Calendar
import java.util.Date
import java.util.UUID

data class ToDo(var name: String, var completed:Boolean, val id:String = UUID.randomUUID().toString(),
                val createdAt: Date = Calendar.getInstance().time)