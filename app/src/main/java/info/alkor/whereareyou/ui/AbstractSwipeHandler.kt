package info.alkor.whereareyou.ui

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

abstract class AbstractSwipeHandler(
        private val deleteIcon: Drawable,
        private val deleteBackground: ColorDrawable
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean = false


    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

        val itemView = viewHolder.itemView
        val iconMarginVertical = (viewHolder.itemView.height - deleteIcon.intrinsicHeight) / 2
        val iconMarginHorizontal = deleteIcon.intrinsicWidth / 2

        if (dX > 0) {
            deleteBackground.setBounds(
                    itemView.left,
                    itemView.top,
                    dX.toInt(),
                    itemView.bottom)
            deleteIcon.setBounds(
                    itemView.left + iconMarginHorizontal,
                    itemView.top + iconMarginVertical,
                    itemView.left + iconMarginHorizontal + deleteIcon.intrinsicWidth,
                    itemView.bottom - iconMarginVertical)
        } else {
            deleteBackground.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom)
            deleteIcon.setBounds(
                    itemView.right - iconMarginHorizontal - deleteIcon.intrinsicWidth,
                    itemView.top + iconMarginVertical,
                    itemView.right - iconMarginHorizontal,
                    itemView.bottom - iconMarginVertical)
            deleteIcon.level = 0
        }

        deleteBackground.draw(c)

        c.save()

        if (dX > 0) {
            c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
        } else {
            c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        }

        deleteIcon.draw(c)

        c.restore()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}