package ru.tretyackov.todo.ui

import com.yandex.div.core.DivActionHandler
import com.yandex.div.core.DivViewFacade
import com.yandex.div.json.expressions.ExpressionResolver
import com.yandex.div2.DivAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class Screen{
    Undefined,
    TodoList
}

class NavigationDivActionHandler : DivActionHandler() {
    private val _openScreenState = MutableStateFlow(Screen.Undefined)
    val openScreenState = _openScreenState.asStateFlow()

    override fun handleAction(
        action: DivAction,
        view: DivViewFacade,
        resolver: ExpressionResolver
    ): Boolean {
        if (super.handleAction(action, view, resolver)) {
            return true
        }

        val uri = action.url?.evaluate(view.expressionResolver) ?: return false
        if (uri.authority != "screen" || uri.scheme != "open") return false
        val screenId = uri.getQueryParameter("id") ?: return false
        if(screenId == "todo_list")
            _openScreenState.update { Screen.TodoList }
        return true
    }
}