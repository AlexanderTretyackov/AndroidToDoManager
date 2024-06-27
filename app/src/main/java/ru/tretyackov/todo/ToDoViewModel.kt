package ru.tretyackov.todo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

private const val TODO_ID = "TODO_ID"

class ToDoViewModel(private val state: SavedStateHandle) : ViewModel(), IToDoViewModel {
    private var toDo : TodoItem? = null
    init{
        val id = state.get<String>(TODO_ID)
        if(id != null)
        {
            toDo = TodoItemsRepository.find(id)
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
            TodoItemsRepository.remove(t)
        }
        goBack()
    }
    override fun saveToDo()
    {
        val t = toDo
        val text = textState.value.trim()
        val deadline = if(isDeadlineState.value) deadlineDateState.value else null
        if(t != null)
            TodoItemsRepository.update(t, TodoItem(text, t.completed, t.id, t.createdAt,
                priority = priorityState.value,
                deadline = deadline))
        else
            TodoItemsRepository.add(TodoItem(text, false,
                priority = priorityState.value,
                deadline = deadline))
        goBack()
    }
    override fun goBack()
    {
        _goBackState.update { true }
    }
}
