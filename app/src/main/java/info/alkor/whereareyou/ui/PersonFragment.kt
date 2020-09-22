package info.alkor.whereareyou.ui

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyoukt.R

class PersonFragment : Fragment() {

    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var myAdapter: PersonRecyclerViewAdapter
    private lateinit var myLayoutManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_person_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                myLayoutManager = LinearLayoutManager(context)
                layoutManager = myLayoutManager

                myAdapter = PersonRecyclerViewAdapter(listener)
                adapter = myAdapter
            }

            val itemTouchHelper = ItemTouchHelper(SwipeHandler())
            itemTouchHelper.attachToRecyclerView(view)
        }

        appContext().personsRepository.all
                .observe(this, PersonsObserver(myAdapter))

        return view
    }

    private fun appContext() = context?.applicationContext as AppContext

    companion object {
        fun newInstance() = PersonFragment()
    }

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
        fun onPersonLocationRequested(person: Person)
    }

    inner class SwipeHandler : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView?,
                            viewHolder: RecyclerView.ViewHolder?,
                            target: RecyclerView.ViewHolder?): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
            val person = (viewHolder as PersonRecyclerViewAdapter.ViewHolder).getBoundPerson()
            if (person != null) {
                with(AlertDialog.Builder(context)) {
                    setTitle(R.string.person_deletion)
                    setMessage(R.string.person_deletion_confirmation_query)
                    setIcon(android.R.drawable.ic_dialog_alert)
                    setPositiveButton(android.R.string.yes) { _, _ ->
                        appContext().personsRepository.removePerson(person)
                    }
                    setNegativeButton(android.R.string.no) { _, _ ->
                        myAdapter.notifyItemChanged(viewHolder.getAdapterPosition())
                    }
                    show()
                }
            }
        }
    }

    inner class PersonsObserver(private val adapter: PersonRecyclerViewAdapter) : Observer<List<Person>> {
        override fun onChanged(new: List<Person>?) {
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

    inner class DiffCallback(private val old: List<Person>, private val new: List<Person>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = areContentsTheSame(oldItemPosition, newItemPosition)
        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = new.size
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = old[oldItemPosition] == new[newItemPosition]
    }
}