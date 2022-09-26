package info.alkor.whereareyou.impl.service.android

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import info.alkor.whereareyou.common.loggingTagOf
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch


class LocationService : Service() {

    private val loggingTag = loggingTagOf("locsvc")
    private val scope = CoroutineScope(Dispatchers.IO)
    private val requests = hashMapOf<Long, Person>()

    @ExperimentalCoroutinesApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (isCompleted()) {
                val notification = NotificationsHelper(applicationContext).createNotification()
                startForeground(1, notification)
                Log.d(loggingTag, "service started in foreground")
            }
            handleRequest(intent)
        } else {
            stopIfCompleted()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(loggingTag, "service destroyed")
        super.onDestroy()
    }

    @ExperimentalCoroutinesApi
    private fun handleRequest(intent: Intent) {
        val person = fromIntent(intent)
        val key = now()
        requests[key] = person

        scope.launch {
            appContext().handleLocationRequestFrom(person)
        }.invokeOnCompletion {
            requests.remove(key)
            stopIfCompleted()
        }
    }

    private fun stopIfCompleted() {
        if (isCompleted()) {
            stopSelf()
        }
    }

    private fun isCompleted() = requests.isEmpty()

    override fun onBind(intent: Intent?): IBinder? = null

    private fun appContext() = applicationContext as AppContext

    private fun now() = System.currentTimeMillis()

    companion object {
        private const val PARAM_NAME = "name"
        private const val PARAM_NUMBER = "number"

        fun fromIntent(intent: Intent): Person {
            val number = intent.getStringExtra(PARAM_NUMBER)
            val name = intent.getStringExtra(PARAM_NAME)
            return Person(PhoneNumber(number!!), name)
        }

        fun toIntent(context: Context, person: Person) = Intent(context, LocationService::class.java).apply {
            putExtra(PARAM_NUMBER, person.phone.toExternalForm())
            putExtra(PARAM_NAME, person.name)
        }
    }
}