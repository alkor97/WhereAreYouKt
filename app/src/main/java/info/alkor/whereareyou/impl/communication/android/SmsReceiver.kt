package info.alkor.whereareyou.impl.communication.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.PhoneNumber
import info.alkor.whereareyou.model.communication.Message

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val ctx = appContext(context)
            Telephony.Sms.Intents.getMessagesFromIntent(intent).map {
                val phoneNumber = PhoneNumber(it.originatingAddress)
                val person = ctx.contactProvider.findName(phoneNumber)
                Message(person, it.messageBody)
            }.forEach {
                ctx.messageReceiver.onReceive(it)
            }
        }
    }

    private fun appContext(context: Context) = context.applicationContext as AppContext
}