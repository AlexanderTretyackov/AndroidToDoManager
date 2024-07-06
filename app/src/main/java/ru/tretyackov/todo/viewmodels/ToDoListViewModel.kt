package ru.tretyackov.todo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.tretyackov.todo.data.DataResult
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.data.TodoItemsRepository
import ru.tretyackov.todo.di.AppComponent
import ru.tretyackov.todo.di.DaggerAppComponent
import ru.tretyackov.todo.utilities.IConnectivityMonitor
import javax.inject.Inject

enum class DataState{
    Uninitialized,Loading,Loaded,Error
}

class ToDoListViewModel @Inject constructor(private val todoItemsRepository: TodoItemsRepository,
                                            private val connectivityMonitor : IConnectivityMonitor) : ViewModel() {
    private val _showWithCompletedState = MutableStateFlow(false)
    val showWithCompletedState = _showWithCompletedState.asStateFlow()

    private val _dataState = MutableStateFlow(DataState.Uninitialized)
    val dataState = _dataState.asStateFlow()

    private val _toDoListState = MutableStateFlow(listOf<TodoItem>())

    private val _toDoListFilteredState = MutableStateFlow(listOf<TodoItem>())
    val toDoListFilteredState = _toDoListFilteredState.asStateFlow()

    private val _completedCountState = MutableStateFlow(0)
    val completedCountState = _completedCountState.asStateFlow()

    init {
        viewModelScope.launch {
            val data = todoItemsRepository.getAll()
            data.collect{ newData ->
                when (newData) {
                    is DataResult.Loading -> {
                        _dataState.update { DataState.Loading }
                    }
                    is DataResult.Success -> {
                        val toDoList = newData.data ?: listOf()
                        _toDoListState.update { toDoList }
                        _toDoListFilteredState.update { getFilterToDos(toDoList, showWithCompletedState.value) }
                        _completedCountState.update { toDoList.count { it.completed } }
                        _dataState.update { DataState.Loaded }
                    }
                    is DataResult.Error -> {
                        _dataState.update { DataState.Error }
                    }
                }
            }
        }
        viewModelScope.launch {
            connectivityMonitor.isAvailableFlow.collect{ isAvailable ->
                if(isAvailable && dataState.value == DataState.Error)
                {
                    refresh()
                }
            }
        }
    }

    private fun getFilterToDos(todos:List<TodoItem>, withCompleted:Boolean) : List<TodoItem>
    {
        return if(!withCompleted) todos.filter { toDo -> !toDo.completed } else todos
    }

    fun filter(withCompleted:Boolean)
    {
        if(withCompleted != showWithCompletedState.value)
        {
            _showWithCompletedState.update { withCompleted }
            _toDoListFilteredState.update { getFilterToDos(_toDoListState.value, withCompleted) }
        }
    }

    suspend fun refresh(){
        todoItemsRepository.refresh()
    }

    fun onSwitchToDoCompleted(toDo:TodoItem)
    {
        val newToDo = toDo.copy()
        viewModelScope.launch {
            todoItemsRepository.update(toDo, newToDo)
        }
    }
}