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

        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_24)!!
        val deleteBackground = ColorDrawable(requireContext().getColor(R.color.deleteBackgroundColor))

        val itemTouchHelper = ItemTouchHelper(SwipeHandler(deleteIcon, deleteBackground))
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

    inner class SwipeHandler(deleteIcon: Drawable, deleteBackground: ColorDrawable) : AbstractSwipeHandler(deleteIcon, deleteBackground) {

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val person = (viewHolder as PersonRecyclerViewAdapter.ViewHolder).getBoundPerson()
            if (person != null) {
                viewModel.removePerson(person, commit = false)
                Snackbar.make(viewHolder.itemView, resources.getString(R.string.deleted), Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) { viewModel.restorePerson(person) }
                        .addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                if (event == DISMISS_EVENT_TIMEOUT) {
                                    viewModel.removePerson(person, commit = true)
                                }
                            }
                        })
                        .show()
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