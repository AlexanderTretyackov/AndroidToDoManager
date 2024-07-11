package ru.tretyackov.todo.data

import ru.tretyackov.todo.data.database.TodoItemEntity
import ru.tretyackov.todo.data.network.dto.ToDoItemDto
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

fun ToDoPriority.toApiString() : String {
    return when (this){
        ToDoPriority.Low -> "low"
        ToDoPriority.Medium -> "basic"
        ToDoPriority.High -> "important"
    }
}

fun String.toDoPriorityFromApiString() : ToDoPriority {
    return when (this){
        "low" -> ToDoPriority.Low
        "basic" -> ToDoPriority.Medium
        "important" -> ToDoPriority.High
        else -> ToDoPriority.Medium
    }
}

fun ToDoPriority.toDatabaseString() = toApiString()
fun String.toDoPriorityFromDatabaseString() = toDoPriorityFromApiString()

fun TodoItem.toDto() : ToDoItemDto {
    return ToDoItemDto(
        id = id,
        text = name,
        importance = priority.toApiString(),
        deadline = deadline?.time,
        done = completed, color = null,
        createdAt = createdAt.time,
        changedAt = lastUpdatedAt.time,
        lastUpdatedBy = "1")
}

fun TodoItem.toDatabaseEntity() : TodoItemEntity {
    return TodoItemEntity(
        id = id,
        text = name,
        done = completed,
        priority = priority.toDatabaseString(),
        deadline = deadline?.time,
        createdAt = createdAt.time,
        lastUpdatedAt = lastUpdatedAt.time,
        operation = null)
}

fun Date.toFormattedString() : String{
    val pattern = "d MMMM yyyy"
    val simpleDateFormat = SimpleDateFormat(pattern)
    return simpleDateFormat.format(this)
}