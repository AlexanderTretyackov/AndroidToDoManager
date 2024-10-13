package ru.tretyackov.todo.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.tretyackov.todo.di.AppScope

@AppScope
@Dao
interface TodoDao {
    @Query("SELECT * FROM todoList WHERE unsavedOperation IS NOT 'Deleted'")
    fun getAllExceptDeletedFlow(): Flow<List<TodoItemEntity>>

    @Query("SELECT * FROM todoList WHERE unsavedOperation IS NOT NULL")
    suspend fun getAllWithOperation(): List<TodoItemEntity>

    @Query("SELECT * FROM todoList WHERE id = :id")
    suspend fun getById(id: String): TodoItemEntity?

    @Insert
    suspend fun add(entity: TodoItemEntity)

    @Update
    suspend fun update(entity: TodoItemEntity)

    @Insert
    suspend fun insertAll(entities: List<TodoItemEntity>)

    @Query("DELETE FROM todoList WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM todoList")
    suspend fun deleteAll()

    @Query("UPDATE todoList SET unsavedOperation = 'Created' WHERE id = :id")
    suspend fun markToDoCreated(id: String)

    @Query("UPDATE todoList SET unsavedOperation = 'Updated' WHERE id = :id")
    suspend fun markToDoUpdated(id: String)

    @Query("UPDATE todoList SET unsavedOperation = 'Deleted' WHERE id = :id")
    suspend fun markToDoDeleted(id: String)

    @Query("UPDATE todoList SET unsavedOperation = NULL WHERE id = :id")
    suspend fun unmarkToDo(id: String)
}
