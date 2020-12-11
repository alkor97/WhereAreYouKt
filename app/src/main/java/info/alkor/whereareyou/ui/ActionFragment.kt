package info.alkor.whereareyou.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        val itemTouchHelper = ItemTouchHelper(SwipeHandler())
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

    inner class SwipeHandler : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val messageId = (viewHolder as ActionRecyclerViewAdapter.ViewHolder).getBoundObjectId()
            if (messageId != null) {
                with(AlertDialog.Builder(context)) {
                    setTitle(R.string.action_deletion)
                    setMessage(R.string.action_deletion_confirmation_query)
                    setIcon(android.R.drawable.ic_dialog_alert)
                    setPositiveButton(android.R.string.yes) { _, _ ->
                        viewModel.removeAction(messageId)
                    }
                    setNegativeButton(android.R.string.no) { _, _ ->
                        action_list.adapter?.notifyItemChanged(viewHolder.getAdapterPosition())
                    }
                    show()
                }
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
