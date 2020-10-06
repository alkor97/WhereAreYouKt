package info.alkor.whereareyou.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import info.alkor.whereareyou.R
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import info.alkor.whereareyou.databinding.LayoutPersonBinding
import info.alkor.whereareyou.model.action.Person
import java.util.*

class PersonRecyclerViewAdapter(private val listener: PersonFragment.OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<PersonRecyclerViewAdapter.ViewHolder>() {

    private val items = ArrayList<Person>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: LayoutPersonBinding = DataBindingUtil.inflate(inflater,
                R.layout.layout_person,
                parent,
                false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<Person>) {
        this.items.clear()
        this.items.addAll(items)
    }

    fun getItems(): List<Person> = this.items

    inner class ViewHolder(private val binding: LayoutPersonBinding) : RecyclerView.ViewHolder(binding.root) {

        private var boundPerson: Person? = null
        private val builder = PersonViewModel.Builder(binding.root.context)

        fun bind(person: Person) {
            boundPerson = person
            binding.model = builder.build(binding.model, person)
            binding.root.setOnClickListener { _ -> listener?.onPersonLocationRequested(person) }
        }

        fun getBoundPerson() = boundPerson
    }
}
