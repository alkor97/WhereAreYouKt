package info.alkor.whereareyou.ui

import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import info.alkor.whereareyou.R
import info.alkor.whereareyou.databinding.LayoutActionBinding
import info.alkor.whereareyou.model.action.LocationAction
import info.alkor.whereareyou.model.action.MessageId
import info.alkor.whereareyou.ui.ActionFragment.OnListFragmentInteractionListener
import java.util.*

class ActionRecyclerViewAdapter(
        private val listener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<ActionRecyclerViewAdapter.ViewHolder>() {

    private val items = ArrayList<LocationAction>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: LayoutActionBinding = DataBindingUtil.inflate(inflater,
                R.layout.layout_action,
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

    inner class ViewHolder(private val binding: LayoutActionBinding) : RecyclerView.ViewHolder(binding.root) {

        private var boundObjectId: MessageId? = null
        private val builder = LocationActionViewModel.Builder(binding.root.context)

        fun getBoundObjectId() = boundObjectId

        fun bind(action: LocationAction) {
            binding.root.setOnClickListener { listener?.onShowLocation(action) }
            binding.model = builder.build(binding.model, action).apply {
                if (menuVisible == View.VISIBLE) {
                    preparePopup(action)
                }
            }
            boundObjectId = action.id
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
                    true
                }
                popup.show()
            }
        }
    }
}
