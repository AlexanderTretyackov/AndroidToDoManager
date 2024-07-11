package ru.tretyackov.todo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Database(entities = [TodoItemEntity::class], version = 1)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}

@Module
object DatabaseModule{
    @Singleton
    @Provides
    fun provideDatabase(ctx: Context): TodoDao = createTodoDao(ctx)
    private fun createTodoDao(ctx: Context) : TodoDao {
        val db = Room.databaseBuilder(
            ctx,
            TodoDatabase::class.java, "todo-database"
        ).build()
        return db.todoDao()
    }
}