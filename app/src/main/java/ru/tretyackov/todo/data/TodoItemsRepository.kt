package ru.tretyackov.todo.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import ru.tretyackov.todo.di.AppComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoItemsRepository @Inject constructor(private val toDoApi : ToDoListApi)
{
    private var revision : Int = 0
    private val context = newSingleThreadContext("CounterContext")
    private val _todosState = MutableStateFlow<DataResult<List<TodoItem>>>(DataResult.Loading())

    suspend fun getAll() : Flow<DataResult<List<TodoItem>>> {
        CoroutineScope(context).launch{
            _todosState.update { getToDoList() }
        }
        return _todosState
    }

    suspend fun refresh() {
        withContext(context)
        {
            _todosState.update { DataResult.Loading() }
            _todosState.update { getToDoList() }
        }
    }

    private fun getOldList() : List<TodoItem>? {
        val listDataResult = _todosState.value
        if (listDataResult is DataResult.Success) {
           return listDataResult.data
        }
        return null
    }

    private fun updateOldList(newList : List<TodoItem>) {
        _todosState.update { DataResult.Success(newList) }
    }

    suspend fun add(todoItem: TodoItem) : DataResult<TodoItem> {
        return withContext(context){
            try {
                val createdToDoItemDto = toDoApi.add(revision, todoItem.id, CreateToDoItemDto(todoItem.toDto()))
                revision = createdToDoItemDto.revision
                val toDo = createdToDoItemDto.element.toModel()
                val oldList = getOldList()
                if(oldList != null)
                {
                    val newList = oldList.toMutableList()
                    newList.add(toDo)
                    updateOldList(newList)
                }
                return@withContext DataResult.Success(toDo)
            }
            catch (ex: Exception){
                return@withContext DataResult.Error("Error")
            }
        }
    }

    private suspend fun getToDoList() : DataResult<List<TodoItem>> {
        return withContext(context){
            try{
                val toDoListDto = toDoApi.getToDoList()
                revision = toDoListDto.revision
                val listModel = toDoListDto.toModel()
                return@withContext DataResult.Success(listModel)
            }
            catch (ex: Exception){
                return@withContext DataResult.Error("Error")
            }
        }
    }

    suspend fun remove(todoItem: TodoItem) : DataResult<String> {
        return withContext(context){
            try {
                val deletedToDoItemDto = toDoApi.delete(revision, todoItem.id)
                revision = deletedToDoItemDto.revision
                val oldList = getOldList()
                if(oldList != null)
                {
                    val newList = oldList.toMutableList()
                    newList.remove(todoItem)
                    updateOldList(newList)
                }
                return@withContext DataResult.Success("Success")
            }
            catch (ex: Exception){
                return@withContext DataResult.Error("Error")
            }
        }
    }

    suspend fun update(oldTodoItem: TodoItem, newTodoItem: TodoItem) : DataResult<TodoItem> {
        return withContext(context){
            try {
                val updatedToDoItemDto = toDoApi.update(revision, oldTodoItem.id, UpdateToDoItemDto(newTodoItem.toDto()))
                revision = updatedToDoItemDto.revision
                val oldList = getOldList()
                if(oldList != null)
                {
                    val newList = oldList.toMutableList()
                    val indexOldTodoItem = newList.indexOf(oldTodoItem)
                    newList[indexOldTodoItem] = newTodoItem
                    updateOldList(newList)
                }
                return@withContext DataResult.Success(newTodoItem)
            }
            catch (ex: Exception){
                return@withContext DataResult.Error("Error")
            }
        }
    }

    suspend fun find(id:String): TodoItem?{
        return withContext(context) {
           return@withContext getOldList()?.find { toDo -> toDo.id == id }
        }
    }
}
