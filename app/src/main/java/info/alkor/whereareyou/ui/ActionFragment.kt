package info.alkor.whereareyou.ui

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.LocationAction
import info.alkor.whereareyoukt.R

class ActionFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var myAdapter: ActionRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_action_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                myAdapter = ActionRecyclerViewAdapter(listener)
                adapter = myAdapter
            }
        }

        (context?.applicationContext as AppContext).actionsRepository.all
                .observe(this, ActionsObserver(myAdapter))

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
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

    inner class ActionsObserver(private val adapter: ActionRecyclerViewAdapter) : Observer<List<LocationAction>> {
        override fun onChanged(new: List<LocationAction>?) {
            val newList = new ?: ArrayList()
            val result = DiffUtil.calculateDiff(DiffCallback(adapter.getItems(), newList))
            adapter.setItems(newList)
            result.dispatchUpdatesTo(adapter)
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
