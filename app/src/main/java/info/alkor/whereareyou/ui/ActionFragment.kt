package info.alkor.whereareyou.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import info.alkor.whereareyou.R
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.LocationAction

class ActionFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var myAdapter: ActionRecyclerViewAdapter
    private lateinit var myLayoutManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_action_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                myLayoutManager = LinearLayoutManager(context)
                layoutManager = myLayoutManager

                myAdapter = ActionRecyclerViewAdapter(listener)
                adapter = myAdapter
            }

            val itemTouchHelper = ItemTouchHelper(SwipeHandler())
            itemTouchHelper.attachToRecyclerView(view)
        }

        appContext().actionsRepository.all
                .observe(viewLifecycleOwner, ActionsObserver(myAdapter))

        return view
    }

    private fun appContext() = context?.applicationContext as AppContext

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
                        appContext().actionsRepository.remove(messageId)
                    }
                    setNegativeButton(android.R.string.no) { _, _ ->
                        myAdapter.notifyItemChanged(viewHolder.getAdapterPosition())
                    }
                    show()
                }
            }
        }
    }

    inner class ActionsObserver(private val adapter: ActionRecyclerViewAdapter) : Observer<List<LocationAction>> {
        override fun onChanged(new: List<LocationAction>?) {
            val newList = new ?: ArrayList()
            val result = DiffUtil.calculateDiff(DiffCallback(adapter.getItems(), newList), false)
            val oldListSize = adapter.getItems().size

            adapter.setItems(newList)
            result.dispatchUpdatesTo(adapter)

            if (newList.size > oldListSize) {
                myLayoutManager.scrollToPosition(0)
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
