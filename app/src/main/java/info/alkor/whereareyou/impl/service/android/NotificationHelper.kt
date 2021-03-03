package info.alkor.whereareyou.impl.service.android

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import info.alkor.whereareyou.R
import info.alkor.whereareyou.ui.SimpleActivity

class NotificationsHelper(val context: Context) {
    private val CHANNEL_ID = "Location Provider Channel"

    fun createNotification(): Notification {
        createNotificationChannel()

        val intent = Intent(context, SimpleActivity::class.java)
//                .apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        //val icon = BitmapFactory.decodeResource(context.resources, R.drawable.dog)

        //NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_whereareyou_icon)
                //.setLargeIcon(icon)
                .setContentTitle(context.getText(R.string.app_name))
                .setContentText(context.getText(R.string.location_in_progress))
//                .setStyle(
//                        NotificationCompat.BigPictureStyle()
//                                .bigPicture(icon)
//                                .bigLargeIcon(null))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_ID
            val descriptionText = "Channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}