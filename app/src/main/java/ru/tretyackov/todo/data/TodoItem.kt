package ru.tretyackov.todo.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.UUID

enum class ToDoPriority{
    Low,Medium,High
}

fun ToDoPriority.toApiString() : String {
    return when (this){
        ToDoPriority.Low -> "low"
        ToDoPriority.Medium -> "basic"
        ToDoPriority.High -> "important"
    }
}

data class TodoItem(var name: String, var completed:Boolean, val id:String = UUID.randomUUID().toString(),
                    val createdAt: Date = Calendar.getInstance().time, var lastUpdatedAt: Date = createdAt,
                    var priority: ToDoPriority = ToDoPriority.Medium, var deadline:Date? = null)

fun Date.toFormattedString() : String{
    val pattern = "d MMMM yyyy"
    val simpleDateFormat = SimpleDateFormat(pattern)
    return simpleDateFormat.format(this)
}

sealed class DataResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Loading<T>() : DataResult<T>()
    class Success<T>(data: T) : DataResult<T>(data = data)
    class Error<T>(errorMessage: String) : DataResult<T>(message = errorMessage)
}
