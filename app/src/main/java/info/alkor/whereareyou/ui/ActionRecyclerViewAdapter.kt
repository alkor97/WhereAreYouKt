package info.alkor.whereareyou.ui

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import info.alkor.whereareyou.model.action.LocationAction
import info.alkor.whereareyou.ui.ActionFragment.OnListFragmentInteractionListener
import info.alkor.whereareyoukt.R
import info.alkor.whereareyoukt.databinding.FragmentActionBinding
import java.util.*

class ActionRecyclerViewAdapter(
        private val listener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<ActionRecyclerViewAdapter.ViewHolder>() {

    private val items = ArrayList<LocationAction>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: FragmentActionBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_action,
                parent,
                false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: List<LocationAction>) {
        this.items.clear()
        this.items.addAll(items)
    }

    fun getItems(): List<LocationAction> = this.items

    inner class ViewHolder(private val binding: FragmentActionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(action: LocationAction) {
            val model = LocationActionViewModel(binding.root.context)
            model.render(action)
            binding.model = model

            if (model.menuVisible == View.VISIBLE) {
                preparePopup(action)
            }
        }

        private fun preparePopup(action: LocationAction) {
            binding.detailsPopup.setOnClickListener { view ->
                val popup = PopupMenu(binding.root.context, view)
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.menu_location_popup, popup.menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_share -> listener?.onShareLocation(action)
                        R.id.action_show -> listener?.onShowLocation(action)
                    }
                    false
                }
                popup.show()
            }
        }
    }
}
