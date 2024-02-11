package ru.tretyackov.todo

import java.util.Calendar
import java.util.Date

data class ToDo(val id:String, var name: String, var completed:Boolean,
           val createdAt: Date = Calendar.getInstance().time)