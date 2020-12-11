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
import info.alkor.whereareyou.model.action.Person
import kotlinx.android.synthetic.main.fragment_person_list.*

class PersonFragment : Fragment() {

    private lateinit var viewModel: PersonListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_person_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PersonListViewModel::class.java)
        val myAdapter = PersonRecyclerViewAdapter(activity as OnListFragmentInteractionListener)

        person_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myAdapter
        }

        val itemTouchHelper = ItemTouchHelper(SwipeHandler())
        itemTouchHelper.attachToRecyclerView(person_list)

        observePersons(myAdapter)
    }

    private fun observePersons(myAdapter: PersonRecyclerViewAdapter) {
        viewModel.persons.observe(viewLifecycleOwner) { newList ->
            val result = DiffUtil.calculateDiff(DiffCallback(myAdapter.getItems(), newList), true)
            val oldListSize = myAdapter.getItems().size

            myAdapter.setItems(newList)
            result.dispatchUpdatesTo(myAdapter)

            if (newList.size > oldListSize) {
                person_list.layoutManager?.scrollToPosition(0)
            }
        }
    }

    companion object {
        fun newInstance() = PersonFragment()
    }

    interface OnListFragmentInteractionListener {
        fun onPersonLocationRequested(person: Person)
    }

    inner class SwipeHandler : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val person = (viewHolder as PersonRecyclerViewAdapter.ViewHolder).getBoundPerson()
            if (person != null) {
                with(AlertDialog.Builder(context)) {
                    setTitle(R.string.person_deletion)
                    setMessage(R.string.person_deletion_confirmation_query)
                    setIcon(android.R.drawable.ic_dialog_alert)
                    setPositiveButton(android.R.string.yes) { _, _ ->
                        viewModel.removePerson(person)
                    }
                    setNegativeButton(android.R.string.no) { _, _ ->
                        person_list.adapter?.notifyItemChanged(viewHolder.getAdapterPosition())
                    }
                    show()
                }
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