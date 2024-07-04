package ru.tretyackov.todo.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.tretyackov.todo.data.DataResult
import ru.tretyackov.todo.utilities.DateHelper
import ru.tretyackov.todo.data.ToDoPriority
import ru.tretyackov.todo.data.TodoItem
import ru.tretyackov.todo.data.TodoItemsRepository
import java.util.Date

private const val TODO_ID = "TODO_ID"

interface IToDoViewModel{
    val textState: StateFlow<String>
    val priorityState: StateFlow<ToDoPriority>
    val isDeadlineState: StateFlow<Boolean>
    val deadlineDateState: StateFlow<Date>
    val deletableState: StateFlow<Boolean>
    val isLoadingState: StateFlow<Boolean>
    val errorState: StateFlow<ToDoUpdateError>
    fun updateText(text: String)
    fun updatePriority(priority: ToDoPriority)
    fun updateIsDeadline(isDeadline:Boolean)
    fun updateDeadlineDate(date: Date)
    fun deleteToDo()
    fun saveToDo()
    fun goBack()
}

enum class ToDoUpdateError{
    None,
    EmptyText,
    Network,
}

class ToDoViewModel(private val state: SavedStateHandle) : ViewModel(), IToDoViewModel {
    private var toDo : TodoItem? = null
    init{
        val id = state.get<String>(TODO_ID)
        if(id != null)
        {
            viewModelScope.launch {
                toDo = TodoItemsRepository.find(id)
            }
        }
    }

    fun updateTodoItem(t : TodoItem)
    {
        //TODO: сохранение измененных данных
        state[TODO_ID] = t.id
        toDo = t
        _deletableState.update { true }
        _textState.update { t.name }
        _priorityState.update { t.priority }
        _isDeadlineState.update { t.deadline != null }
        if(t.deadline != null)
            _deadlineDateState.update { t.deadline }
    }

    private val _goBackState = MutableStateFlow(false)
    val goBackState = _goBackState.asStateFlow()

    private val _deletableState = MutableStateFlow(false)
    override val deletableState = _deletableState.asStateFlow()

    private val _textState = MutableStateFlow("")
    override val textState = _textState.asStateFlow()

    private val _priorityState = MutableStateFlow(ToDoPriority.Medium)
    override val priorityState = _priorityState.asStateFlow()

    private val _isDeadlineState = MutableStateFlow(false)
    override val isDeadlineState = _isDeadlineState.asStateFlow()

    private val _deadlineDateState = MutableStateFlow(DateHelper.now())
    override val deadlineDateState = _deadlineDateState.asStateFlow()

    private val _isLoadingState = MutableStateFlow(false)
    override val isLoadingState = _isLoadingState.asStateFlow()

    private val _errorState = MutableStateFlow(ToDoUpdateError.None)
    override val errorState = _errorState.asStateFlow()

    override fun updateText(text: String)
    {
        _textState.update { text }
    }
    override fun updatePriority(priority: ToDoPriority)
    {
        _priorityState.update { priority }
    }
    override fun updateIsDeadline(isDeadline:Boolean)
    {
        _isDeadlineState.update { isDeadline }
    }
    override fun updateDeadlineDate(date: Date)
    {
        _deadlineDateState.update { date }
    }
    override fun deleteToDo()
    {
        val t = toDo
        if(t != null)
        {
            viewModelScope.launch {
                _isLoadingState.update { true }
                val removeResult = TodoItemsRepository.remove(t)
                _isLoadingState.update { false }
                when (removeResult) {
                    is DataResult.Loading -> goBack()
                    is DataResult.Success -> goBack()
                    is DataResult.Error -> {
                        _errorState.update { ToDoUpdateError.Network }
                        return@launch
                    }
                }
                goBack()
            }
        }
    }

    override fun saveToDo()
    {
        val oldToDo = toDo
        val text = textState.value.trim()
        if(text.isEmpty())
        {
            _errorState.update { ToDoUpdateError.EmptyText }
            return
        }
        val deadline = if(isDeadlineState.value) deadlineDateState.value else null
        viewModelScope.launch {
            if (oldToDo != null)
            {
                val newToDoItem = oldToDo.copy()
                newToDoItem.name = text
                newToDoItem.priority = priorityState.value
                newToDoItem.deadline = deadline
                _isLoadingState.update { true }
                val result = TodoItemsRepository.update(oldToDo, newToDoItem)
                _isLoadingState.update { false }
                if(result is DataResult.Error)
                {
                    _errorState.update { ToDoUpdateError.Network }
                    return@launch
                }
            }
            else
            {
                _isLoadingState.update { true }
                val result = TodoItemsRepository.add(
                    TodoItem(
                        text, false,
                        priority = priorityState.value,
                        deadline = deadline
                    )
                )
                _isLoadingState.update { false }
                if(result is DataResult.Error)
                {
                    _errorState.update { ToDoUpdateError.Network }
                    return@launch
                }
            }
            goBack()
        }
    }
    override fun goBack()
    {
        _goBackState.update { true }
    }
}
