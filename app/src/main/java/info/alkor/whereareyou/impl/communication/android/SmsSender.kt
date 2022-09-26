package info.alkor.whereareyou.impl.communication.android

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import info.alkor.whereareyou.impl.communication.AbstractMessageSender
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.SendingStatus
import info.alkor.whereareyou.model.action.finishesSending
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class SmsSender(private val context: Context) : AbstractMessageSender(context as AppContext) {

    private val scope = CoroutineScope(Dispatchers.IO)

    override suspend fun sendMessage(person: Person, message: String, channel: Channel<SendingStatus>) {
        channel.send(SendingStatus.PENDING)
        val localChannel = Channel<SendingStatus>()

        val (sendReceiver, sendIntent) = createAndRegisterIntent(SMS_SENT, localChannel)
        val (deliveryReceiver, deliveryIntent) = createAndRegisterIntent(SMS_DELIVERED, localChannel)

        getSmsManager().sendTextMessage(
                person.phone.value,
                null,
                message,
                sendIntent,
                deliveryIntent)

        for (status in localChannel) {
            channel.send(status)
        }

        context.unregisterReceiver(sendReceiver)
        context.unregisterReceiver(deliveryReceiver)

        channel.close()
    }

    private data class ReceiverAndIntent(val receiver: BroadcastReceiver, val intent: PendingIntent)

    private fun createAndRegisterIntent(action: String, channel: Channel<SendingStatus>): ReceiverAndIntent {
        val intent = Intent(action)
        val receiver = MessageEventReceiver(scope, channel)
        context.registerReceiver(receiver, IntentFilter(action))
        return ReceiverAndIntent(
            receiver,
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        )
    }

    private class MessageEventReceiver(val scope: CoroutineScope, val channel: Channel<SendingStatus>) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val status = getStatus(resultCode, intent.action!!)
                if (status != null) {
                    scope.launch {
                        channel.send(status)
                        if (status.finishesSending()) {
                            channel.close()
                        }
                    }
                }
            }
        }
    }

    private companion object {
        private const val SMS_SENT = "info.alkor.whereareyou.SMS_SENT"
        private const val SMS_DELIVERED = "info.alkor.whereareyou.SMS_DELIVERED"

        private fun getStatus(resultCode: Int, action: String) = when (resultCode to action) {
            Pair(Activity.RESULT_OK, SMS_SENT) -> SendingStatus.SENT
            Pair(Activity.RESULT_OK, SMS_DELIVERED) -> SendingStatus.DELIVERED
            Pair(Activity.RESULT_CANCELED, SMS_SENT) -> SendingStatus.SENDING_FAILED
            Pair(Activity.RESULT_CANCELED, SMS_DELIVERED) -> SendingStatus.DELIVERY_FAILED
            else -> null
        }
    }

    private fun getSmsManager() = context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
}