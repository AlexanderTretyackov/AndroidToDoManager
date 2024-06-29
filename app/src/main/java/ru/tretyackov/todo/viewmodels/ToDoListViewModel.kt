package ru.tretyackov.todo.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.data.TodoItemsRepository

class ToDoListViewModel : ViewModel() {
    val data = TodoItemsRepository.getAll()
    private val _showWithCompletedState = MutableStateFlow(false)
    val showWithCompletedState = _showWithCompletedState.asStateFlow()

    private val _toDoListState = MutableStateFlow(listOf<TodoItem>())
    val toDoListState = _toDoListState.asStateFlow()

    private val _completedCountState = MutableStateFlow(0)
    val completedCountState = _completedCountState.asStateFlow()

    init {
        viewModelScope.launch {
            data.collect{ newData ->
                _toDoListState.update { getFilterToDos(newData, showWithCompletedState.value) }
                _completedCountState.update { newData.count { it.completed } }
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
            _toDoListState.update { getFilterToDos(data.value, withCompleted) }
        }
    }

    fun onSwitchToDoCompleted(toDo:TodoItem)
    {
        val currentCountCompleted = _completedCountState.value
        _completedCountState.update { currentCountCompleted + if(toDo.completed) 1 else -1 }
        _toDoListState.update { getFilterToDos(data.value, showWithCompletedState.value) }
    }
}