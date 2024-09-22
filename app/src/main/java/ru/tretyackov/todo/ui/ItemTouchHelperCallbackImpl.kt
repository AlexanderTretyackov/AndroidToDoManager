package ru.tretyackov.todo.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import ru.tretyackov.todo.R
import ru.tretyackov.todo.data.TodoItem
import kotlin.math.min

private const val CHECKED_ICON_LEFT_OFFSET_DP = 10
private const val CHECKED_ICON_THRESHOLD_FULL_OPAQUE = 0.7f
private const val MAX_WIDTH_CHECK_VIEW_DP = 50

class ItemTouchHelperCallbackImpl(
    context: Context,
    private val topBottomItemPaddingDp: Int,
    private val onSwitchToDoItemCompleted: (TodoItem) -> Unit
) : ItemTouchHelper.Callback() {
    private val mapItemsForChangeCompleted = mutableSetOf<String>()
    private val density = context.resources.displayMetrics.density
    private val maxWidthCheckView = MAX_WIDTH_CHECK_VIEW_DP * density
    private val drawableCheckBackground by lazy {
        ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.checked_background,
            null
        )
    }
    private val drawableCheckIcon = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.checked_icon,
        null
    )

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeFlag(ACTION_STATE_SWIPE, RIGHT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 1f
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return Float.MAX_VALUE
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val constrainedDx = min(dX, maxWidthCheckView)
        val toDoItem = (viewHolder as ToDoViewHolder).toDoItem
        if (toDoItem == null) {
            super.onChildDraw(
                c,
                recyclerView,
                viewHolder,
                constrainedDx,
                dY,
                actionState,
                isCurrentlyActive
            )
            return
        }
        val boundRightSwipe = constrainedDx == maxWidthCheckView
        if (boundRightSwipe)
            mapItemsForChangeCompleted.add(toDoItem.id)
        if (constrainedDx <= 0 && mapItemsForChangeCompleted.contains(toDoItem.id)) {
            mapItemsForChangeCompleted.remove(toDoItem.id)
            onSwitchToDoItemCompleted(toDoItem)
        }
        val topBackground =
            (viewHolder.itemView.top - topBottomItemPaddingDp * density).toInt()
        val bottomBackground =
            (viewHolder.itemView.bottom + topBottomItemPaddingDp * density).toInt()
        drawableCheckBackground?.setBounds(
            0,
            topBackground,
            constrainedDx.toInt(),
            bottomBackground,
        )
        drawableCheckBackground?.draw(c)
        val checkedIconWidth = drawableCheckIcon!!.intrinsicWidth
        val checkedIconHeight = drawableCheckIcon.intrinsicHeight
        val checkedIconTop =
            topBackground + (bottomBackground - topBackground) / 2 - checkedIconHeight / 2
        val checkedIconBottom = checkedIconTop + checkedIconHeight
        val checkedIconLeft = (CHECKED_ICON_LEFT_OFFSET_DP * density).toInt()
        val checkedIconVisibleWidth = if (constrainedDx <= checkedIconLeft) 0 else min(
            checkedIconWidth,
            (constrainedDx - checkedIconLeft).toInt()
        )
        val checkedIconRight = checkedIconLeft + checkedIconVisibleWidth
        if (constrainedDx <= checkedIconLeft)
            drawableCheckIcon.alpha = 0
        else {
            val alpha =
                (255 * (constrainedDx - checkedIconLeft) / (checkedIconWidth * CHECKED_ICON_THRESHOLD_FULL_OPAQUE)).toInt()
            drawableCheckIcon.alpha = if (alpha > 255) 255 else alpha
        }
        val bitmapCheckIcon = drawableCheckIcon.toBitmap()
        c.drawBitmap(
            bitmapCheckIcon,
            Rect(
                0,
                0,
                checkedIconVisibleWidth,
                checkedIconHeight
            ),
            Rect(checkedIconLeft, checkedIconTop, checkedIconRight, checkedIconBottom),
            null
        )
        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            constrainedDx,
            dY,
            actionState,
            isCurrentlyActive
        )
    }
}