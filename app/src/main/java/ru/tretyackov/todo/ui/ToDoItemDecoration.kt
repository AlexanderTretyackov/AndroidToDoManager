package ru.tretyackov.todo.ui

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ru.tretyackov.todo.data.TodoItem

class ToDoItemDecoration(
    context: Context,
    onSwitchToDoItemCompleted: (TodoItem) -> Unit,
    startEndItemPaddingDp: Int = 0,
    topBottomItemPaddingDp: Int = 0,
) : ItemTouchHelper(
    ItemTouchHelperCallbackImpl(
        context,
        topBottomItemPaddingDp,
        onSwitchToDoItemCompleted
    )
) {
    private val startEndItemPaddingPx =
        (startEndItemPaddingDp * context.resources.displayMetrics.density).toInt()
    private val topBottomItemPaddingPx =
        (topBottomItemPaddingDp * context.resources.displayMetrics.density).toInt()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(
            startEndItemPaddingPx,
            topBottomItemPaddingPx,
            startEndItemPaddingPx,
            topBottomItemPaddingPx
        )
    }
}