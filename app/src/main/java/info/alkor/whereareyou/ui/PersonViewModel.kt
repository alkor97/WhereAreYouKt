package info.alkor.whereareyou.ui

import android.content.Context
import android.util.Log
import info.alkor.whereareyou.R
import info.alkor.whereareyou.model.action.Person

class PersonViewModel {

    var name: String = ""
    var phone: String = ""

    class Builder(context: Context) {
        private val resources = context.resources

        fun build(inputModel: PersonViewModel?, person: Person): PersonViewModel {
            val model = inputModel ?: PersonViewModel()
            model.name = person.name ?: resources.getString(R.string.person_unknown)
            model.phone = resources.getString(R.string.phone, person.phone.toHumanReadable())
            Log.i("render", "$person")
            return model
        }
    }
}
