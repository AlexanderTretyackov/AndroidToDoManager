package ru.tretyackov.todo.data.network.dto

import com.google.gson.annotations.SerializedName
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.data.toDoPriorityFromApiString
import java.util.Date

data class ToDoItemDto (
    val id:String,
    val text: String,
    val importance:String,
    val deadline: Long?,
    val done: Boolean,
    val color: String?,
    @SerializedName("created_at")
    val createdAt:Long,
    @SerializedName("changed_at")
    val changedAt:Long,
    @SerializedName("last_updated_by")
    val lastUpdatedBy: String){
}

fun ToDoItemDto.toModel() : TodoItem {
    return TodoItem(
        id = id,
        name = text,
        deadline = if(deadline!=null) Date(deadline) else null,
        completed = done,
        priority = importance.toDoPriorityFromApiString(),
        createdAt = Date(createdAt),
        lastUpdatedAt = Date(changedAt))
}