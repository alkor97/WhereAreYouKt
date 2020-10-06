package info.alkor.whereareyou.impl.communication.android

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import info.alkor.whereareyou.api.communication.SendingStatusCallback
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.impl.communication.MessageSenderImpl
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.SendingStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SmsSender(private val context: Context) : MessageSenderImpl(context as AppContext) {

    override fun sendMessage(person: Person, message: String, callback: SendingStatusCallback) {
        callback(SendingStatus.PENDING)

        val sentIntent = createAndRegisterIntent(SMS_SENT, callback, arrayOf(SendingStatus.SENT, SendingStatus.SENDING_FAILED))
        val deliveredIntent = createAndRegisterIntent(SMS_DELIVERED, callback, arrayOf(SendingStatus.DELIVERED, SendingStatus.DELIVERY_FAILED))

        SmsManager.getDefault().sendTextMessage(
                person.phone.value,
                null,
                message,
                sentIntent,
                deliveredIntent)
    }

    private fun createAndRegisterIntent(action: String, callback: SendingStatusCallback, terminators: Array<SendingStatus>): PendingIntent? {
        val intent = Intent(action)
        context.registerReceiver(MessageEventReceiver(callback, terminators), IntentFilter(action))
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private class MessageEventReceiver(val callback: SendingStatusCallback, val terminators: Array<SendingStatus>) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val status = getStatus(resultCode, intent.action!!)
                if (status != null) {
                    GlobalScope.launch {
                        callback(status)
                        if (status in terminators) {
                            context?.unregisterReceiver(this@MessageEventReceiver)
                        }
                    }
                }
            }
        }

        private fun getStatus(resultCode: Int, action: String) = when (resultCode to action) {
            Pair(Activity.RESULT_OK, SmsSender.SMS_SENT) -> SendingStatus.SENT
            Pair(Activity.RESULT_OK, SmsSender.SMS_DELIVERED) -> SendingStatus.DELIVERED
            Pair(Activity.RESULT_CANCELED, SmsSender.SMS_SENT) -> SendingStatus.SENDING_FAILED
            Pair(Activity.RESULT_CANCELED, SmsSender.SMS_DELIVERED) -> SendingStatus.DELIVERY_FAILED
            else -> null
        }
    }

    private companion object {
        private const val SMS_SENT = "info.alkor.whereareyou.SMS_SENT"
        private const val SMS_DELIVERED = "info.alkor.whereareyou.SMS_DELIVERED"
    }
}