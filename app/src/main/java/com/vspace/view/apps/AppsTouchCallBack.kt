package com.vspace.view.apps

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * [ItemTouchHelper.Callback] implementation for enabling drag-and-drop reordering
 * in the apps grid RecyclerView.
 *
 * Supports movement in all four directions (up, down, left, right) and
 * delegates the move event to the provided [onMoveBlock] callback.
 *
 * @property onMoveBlock callback invoked with the from and to adapter positions when an item is dragged.
 */
class AppsTouchCallBack(private val onMoveBlock: (from: Int, to: Int) -> Unit) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition
        onMoveBlock(fromPosition, toPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }
}
