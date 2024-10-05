package ru.tretyackov.todo.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.data.toDoPriorityFromDatabaseString
import java.util.Date

enum class TodoCachedOperationType {
    Created,
    Updated,
    Deleted,
}

private fun String.toTodoCachedOperationType(): TodoCachedOperationType? {
    return when (this) {
        "Created" -> TodoCachedOperationType.Created
        "Updated" -> TodoCachedOperationType.Updated
        "Deleted" -> TodoCachedOperationType.Deleted
        else -> null
    }
}

@Entity(tableName = "todoList")
data class TodoItemEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "done")
    val done: Boolean,
    @ColumnInfo(name = "priority")
    val priority: String,
    @ColumnInfo(name = "deadline")
    val deadline: Long?,
    @ColumnInfo(name = "createdAt")
    val createdAt: Long,
    @ColumnInfo(name = "lastUpdatedAt")
    val lastUpdatedAt: Long,
    @ColumnInfo(name = "unsavedOperation")
    val operation: String?,
) {
    val operationType get() = operation?.toTodoCachedOperationType()
}

fun TodoItemEntity.toModel(): TodoItem {
    return TodoItem(
        id = id,
        name = text,
        completed = done,
        createdAt = Date(createdAt),
        lastUpdatedAt = Date(lastUpdatedAt),
        priority = priority.toDoPriorityFromDatabaseString(),
        deadline = if (deadline != null) Date(deadline) else null
    )
}
