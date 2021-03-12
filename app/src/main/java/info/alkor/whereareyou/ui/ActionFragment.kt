package info.alkor.whereareyou.ui

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import info.alkor.whereareyou.R
import info.alkor.whereareyou.model.action.LocationAction
import kotlinx.android.synthetic.main.fragment_action_list.*

class ActionFragment : Fragment() {

    private lateinit var viewModel: LocationActionListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_action_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(LocationActionListViewModel::class.java)
        val myAdapter = ActionRecyclerViewAdapter(activity as OnListFragmentInteractionListener)

        action_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myAdapter
        }

        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_48)!!
        val deleteBackground = ColorDrawable(requireContext().getColor(R.color.deleteBackgroundColor))

        val itemTouchHelper = ItemTouchHelper(SwipeHandler(deleteIcon, deleteBackground))
        itemTouchHelper.attachToRecyclerView(action_list)

        observeActions(myAdapter)
    }

    private fun observeActions(adapter: ActionRecyclerViewAdapter) {
        viewModel.actions.observe(viewLifecycleOwner) { newList ->
            val result = DiffUtil.calculateDiff(DiffCallback(adapter.getItems(), newList), true)
            val oldListSize = adapter.getItems().size

            adapter.setItems(newList)
            result.dispatchUpdatesTo(adapter)

            if (newList.size > oldListSize) {
                action_list.layoutManager?.scrollToPosition(0)
            }
        }
    }

    interface OnListFragmentInteractionListener {
        fun onShareLocation(action: LocationAction): Boolean
        fun onShowLocation(action: LocationAction): Boolean
    }

    companion object {
        fun newInstance() = ActionFragment()
    }

    inner class SwipeHandler(deleteIcon: Drawable, deleteBackground: ColorDrawable) : AbstractSwipeHandler(deleteIcon, deleteBackground) {

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val messageId = (viewHolder as ActionRecyclerViewAdapter.ViewHolder).getBoundObjectId()
            if (messageId != null) {
                viewModel.removeAction(messageId, commit = false)
                Snackbar.make(viewHolder.itemView, resources.getString(R.string.deleted), Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) { viewModel.restoreAction(messageId) }
                        .addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                if (event == DISMISS_EVENT_TIMEOUT) {
                                    viewModel.removeAction(messageId, commit = true)
                                }
                            }
                        })
                        .show()
            }
        }
    }

    inner class DiffCallback(private val old: List<LocationAction>, private val new: List<LocationAction>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = old[oldItemPosition].id == new[newItemPosition].id
        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = new.size
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldElement = old[oldItemPosition]
            val newElement = new[newItemPosition]
            return oldElement.id == newElement.id && oldElement.direction == newElement.direction
                    && oldElement.person == newElement.person && oldElement.location == newElement.location
                    && oldElement.final == newElement.final && oldElement.status == newElement.status
        }
    }
}
