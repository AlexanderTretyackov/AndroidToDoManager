package ru.tretyackov.todo

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.UUID

enum class ToDoPriority{
    Low,Medium,High
}
data class TodoItem(var name: String, var completed:Boolean, val id:String = UUID.randomUUID().toString(),
                    val createdAt: Date = Calendar.getInstance().time, var lastUpdatedAt: Date = createdAt,
                    var priority: ToDoPriority = ToDoPriority.Medium, var deadline:Date? = null)

fun Date.toFormattedString() : String{
    val pattern = "d MMMM yyyy"
    val simpleDateFormat = SimpleDateFormat(pattern)
    return simpleDateFormat.format(this)
}