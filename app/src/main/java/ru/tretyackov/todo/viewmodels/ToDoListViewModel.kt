package ru.tretyackov.todo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.tretyackov.todo.data.RefreshState
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.data.TodoItemsRepository
import ru.tretyackov.todo.utilities.DateHelper
import javax.inject.Inject

enum class DataState {
    Uninitialized, Loading, Loaded, Error
}

class ToDoListViewModel @Inject constructor(private val todoItemsRepository: TodoItemsRepository) :
    ViewModel() {
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
            val toDoListFlow = todoItemsRepository.getAll()
            toDoListFlow.collect { toDoList ->
                _toDoListState.update { toDoList }
                _toDoListFilteredState.update {
                    getFilterToDos(
                        toDoList,
                        showWithCompletedState.value
                    )
                }
                _completedCountState.update {
                    toDoList.count { it.completed }
                }
            }
        }
        viewModelScope.launch {
            todoItemsRepository.refreshState.collect { refreshState ->
                when (refreshState) {
                    RefreshState.CachedLoading -> _dataState.update { DataState.Loading }
                    RefreshState.CachedError -> _dataState.update { DataState.Error }
                    RefreshState.Success -> _dataState.update { DataState.Loaded }
                }
            }
        }
    }

    private fun getFilterToDos(todos: List<TodoItem>, withCompleted: Boolean): List<TodoItem> {
        return if (!withCompleted) todos.filter { toDo -> !toDo.completed } else todos
    }

    fun filter(withCompleted: Boolean) {
        if (withCompleted != showWithCompletedState.value) {
            _showWithCompletedState.update { withCompleted }
            _toDoListFilteredState.update { getFilterToDos(_toDoListState.value, withCompleted) }
        }
    }

    suspend fun refresh() {
        todoItemsRepository.refresh()
    }

    fun onSwitchToDoCompleted(toDo: TodoItem) {
        val newToDo = toDo.copy()
        newToDo.completed = !newToDo.completed
        _completedCountState.update { _completedCountState.value + if (toDo.completed) 1 else -1 }
        _toDoListFilteredState.update {
            getFilterToDos(
                _toDoListState.value,
                showWithCompletedState.value
            )
        }
        viewModelScope.launch {
            todoItemsRepository.update(toDo, newToDo)
        }
    }
}