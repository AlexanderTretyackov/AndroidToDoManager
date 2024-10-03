package ru.tretyackov.todo.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.tretyackov.todo.data.database.TodoCachedOperationType
import ru.tretyackov.todo.data.database.TodoDao
import ru.tretyackov.todo.data.database.TodoItemEntity
import ru.tretyackov.todo.data.database.toModel
import ru.tretyackov.todo.data.network.DataResult
import ru.tretyackov.todo.data.network.ToDoListApi
import ru.tretyackov.todo.data.network.dto.CreateToDoItemDto
import ru.tretyackov.todo.data.network.dto.PatchToDoListDto
import ru.tretyackov.todo.data.network.dto.ToDoItemDto
import ru.tretyackov.todo.data.network.dto.UpdateToDoItemDto
import ru.tretyackov.todo.data.network.dto.toModel
import ru.tretyackov.todo.utilities.ConnectivityMonitor
import ru.tretyackov.todo.utilities.DateHelper
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

enum class RefreshState {
    CachedLoading,
    CachedError,
    Success,
}

private const val SERVER_UNSYNCHRONIZED_DATA_ERROR = "unsynchronized data"

@Singleton
class TodoItemsRepository @Inject constructor(
    private val toDoApi: ToDoListApi,
    private val connectivityMonitor: ConnectivityMonitor,
    private val todoDao: TodoDao
) {
    private var revision: Int = 0
    private val context = Dispatchers.IO
    private val _todosState = MutableStateFlow<List<TodoItem>>(listOf())
    private val _refreshState = MutableStateFlow(RefreshState.CachedLoading)
    val refreshState = _refreshState.asStateFlow()

    init {
        CoroutineScope(context).launch {
            _refreshState.update { RefreshState.CachedLoading }
            todoDao.getAllExceptDeletedFlow().collect { newDatabaseList ->
                _todosState.update { newDatabaseList.map { toDoItemEntity -> toDoItemEntity.toModel() } }
            }
        }
        CoroutineScope(context).launch {
            connectivityMonitor.isAvailableFlow.collect { isAvailable ->
                if (isAvailable && refreshState.value != RefreshState.CachedLoading) {
                    Log.i("TodoItemsRepository", "connectivityMonitor")
                    refresh()
                }
            }
        }
    }

    suspend fun getAll(): Flow<List<TodoItem>> {
        CoroutineScope(context).launch {
            Log.i("TodoItemsRepository", "getAll")
            refreshList()
        }
        return _todosState
    }

    private suspend fun refreshList() {
        Log.i("TodoItemsRepository", "refreshList")
        _refreshState.update { RefreshState.CachedLoading }
        val dataResult = getToDoList()
        if (dataResult !is DataResult.Success) {
            _refreshState.update { RefreshState.CachedError }
            return
        }
        val todoApiList = dataResult.data
        if (todoApiList == null) {
            _refreshState.update { RefreshState.CachedError }
            return
        }
        val todoDatabaseList = todoDao.getAllWithOperation()
        val mergedList = mergeDatabaseAndApiLists(todoDatabaseList, todoApiList)
        if (mergedList != todoApiList) {
            val patchResult = safeExecuteRequest {
                toDoApi.patch(
                    revision,
                    PatchToDoListDto(mergedList, revision)
                )
            }
            if (patchResult !is DataResult.Success) {
                _refreshState.update { RefreshState.CachedError }
                return
            }
            if (patchResult.data != null)
                revision = patchResult.data.revision
        }
        todoDao.deleteAll()
        val listModel = mergedList.map { it.toModel() }
        todoDao.insertAll(listModel.map { it.toDatabaseEntity() })
        _refreshState.update { RefreshState.Success }
    }

    private suspend fun mergeDatabaseAndApiLists(
        unsavedToDoList: List<TodoItemEntity>,
        apiList: List<ToDoItemDto>
    ): List<ToDoItemDto> {
        val apiMap = apiList.associateBy { it.id }
        val newApiList = apiList.toMutableList()
        if (unsavedToDoList.isEmpty()) {
            return apiList
        }
        for (unsavedToDo in unsavedToDoList) {
            when (unsavedToDo.operationType) {
                TodoCachedOperationType.Created -> newApiList.add(unsavedToDo.toModel().toDto())
                TodoCachedOperationType.Updated -> {
                    val toDoFromApi = apiMap[unsavedToDo.id]
                    if (toDoFromApi != null) {
                        if (unsavedToDo.lastUpdatedAt > toDoFromApi.changedAt) {
                            val index = apiList.indexOf(toDoFromApi)
                            newApiList[index] = unsavedToDo.toModel().toDto()
                        }
                    } else {
                        newApiList.add(unsavedToDo.toModel().toDto())
                    }
                }

                TodoCachedOperationType.Deleted -> {
                    if (apiMap.containsKey(unsavedToDo.id)) {
                        newApiList.remove(apiList.first { it.id == unsavedToDo.id })
                    }
                }

                null -> {}
            }
        }
        return newApiList
    }

    suspend fun refresh() {
        withContext(context)
        {
            refreshList()
        }
    }

    private suspend fun <T> retryOnUnsynchronized(func: suspend () -> T): T {
        var count = 3
        var exception: Exception? = null
        while (count > 0) {
            try {
                val funcResult = func()
                return funcResult
            } catch (ex: HttpException) {
                val errorString = ex.response()?.errorBody()?.string()
                if (ex.code() == 400 && errorString == SERVER_UNSYNCHRONIZED_DATA_ERROR) {
                    if (count == 1) {
                        throw ex
                    }
                    refreshList()
                    exception = ex
                } else {
                    throw ex
                }
            }
            count--
        }
        throw exception!!
    }

    //TODO: Обработка ситуации отзыва OAuth токена
    private suspend fun <T> safeExecuteRequest(func: suspend () -> T): DataResult<T> {
        try {
            val funcResult = retryOnUnsynchronized(func)
            return DataResult.Success(funcResult)
        } catch (ex: Exception) {
            return when (ex) {
                is SocketTimeoutException, is UnknownHostException -> {
                    Log.i("handle", "UnknownHostException")
                    DataResult.Error.NetworkError("Error")
                }

                is HttpException -> {
                    Log.i("handle", "HttpException $ex")
                    DataResult.Error.AnotherError("Error")
                }

                else -> {
                    Log.i("handle", "another $ex")
                    DataResult.Error.AnotherError("Error")
                }
            }
        }
    }

    suspend fun add(todoItem: TodoItem): DataResult<TodoItem> {
        return withContext(context) {
            todoDao.add(todoItem.toDatabaseEntity())
            todoDao.markToDoCreated(todoItem.id)
            val addResult = safeExecuteRequest {
                val createdToDoItemDto =
                    toDoApi.add(revision, todoItem.id, CreateToDoItemDto(todoItem.toDto()))
                revision = createdToDoItemDto.revision
                return@safeExecuteRequest createdToDoItemDto.element.toModel()
            }
            if (addResult is DataResult.Success) {
                todoDao.unmarkToDo(todoItem.id)
            }
            return@withContext addResult
        }
    }

    private suspend fun getToDoList(): DataResult<List<ToDoItemDto>> {
        return withContext(context) {
            return@withContext safeExecuteRequest {
                val toDoListDto = toDoApi.getToDoList()
                revision = toDoListDto.revision
                return@safeExecuteRequest toDoListDto.list
            }
        }
    }

    suspend fun remove(todoItem: TodoItem): DataResult<Unit> {
        return withContext(context) {
            todoDao.markToDoDeleted(todoItem.id)
            val removeResult = safeExecuteRequest {
                val deletedToDoItemDto = toDoApi.delete(revision, todoItem.id)
                revision = deletedToDoItemDto.revision
                todoDao.deleteById(todoItem.id)
            }
            return@withContext removeResult
        }
    }

    suspend fun update(oldTodoItem: TodoItem, newTodoItem: TodoItem): DataResult<TodoItem> {
        return withContext(context) {
            newTodoItem.lastUpdatedAt = DateHelper.now()
            todoDao.update(newTodoItem.toDatabaseEntity())
            todoDao.markToDoUpdated(newTodoItem.id)
            val updateResult = safeExecuteRequest {
                val updatedToDoItemDto =
                    toDoApi.update(revision, oldTodoItem.id, UpdateToDoItemDto(newTodoItem.toDto()))
                revision = updatedToDoItemDto.revision
                return@safeExecuteRequest newTodoItem
            }
            if (updateResult is DataResult.Success) {
                todoDao.unmarkToDo(newTodoItem.id)
            }
            return@withContext updateResult
        }
    }

    suspend fun find(id: String): TodoItem? {
        return withContext(context) {
            return@withContext todoDao.getById(id)?.toModel()
        }
    }
}
