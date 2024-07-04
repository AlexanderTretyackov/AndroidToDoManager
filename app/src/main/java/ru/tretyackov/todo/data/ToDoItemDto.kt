package ru.tretyackov.todo.data

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ToDoListDto(val status:String, val list: List<ToDoItemDto>, val revision: Int)

fun ToDoListDto.toModel() : List<TodoItem> {
    return list.map { it.toModel() }
}

data class ToDoItemDto (
    val id:String,
    val text: String,
    val importance:String,
    val deadline: Int?,
    val done: Boolean,
    val color: String?,
    @SerializedName("created_at")
    val createdAt:Int,
    @SerializedName("changed_at")
    val changedAt:Int,
    @SerializedName("last_updated_by")
    val lastUpdatedBy: String){
}

fun TodoItem.toDto() : ToDoItemDto {
    return ToDoItemDto(id = id, text = name, importance = priority.toApiString(), deadline = deadline?.time?.toInt(), done = completed, color = null, createdAt = createdAt.time.toInt(), changedAt = lastUpdatedAt.time.toInt(), lastUpdatedBy = "1")
}

fun ToDoItemDto.toModel() : TodoItem {
    return TodoItem(id = id, name = text, completed = done, createdAt = Date(createdAt.toLong()), lastUpdatedAt = Date(changedAt.toLong()))
}