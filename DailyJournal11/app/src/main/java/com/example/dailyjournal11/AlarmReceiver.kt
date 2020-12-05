package com.example.dailyjournal11

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {

    // Function for when the notification is to be sent
    override fun onReceive(context: Context?, intent: Intent?) {
        // Set intent for when user clicks notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val clickIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        // Build the notification
        var builder = NotificationCompat.Builder(context!!, JournalOptions.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Journal Reminder")
            .setContentText("Don't forget to record a journal entry for today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(clickIntent)
            .setAutoCancel(true)

        // Send the notification
        val mNotificationManager = NotificationManagerCompat.from(context)
        mNotificationManager.notify(1, builder.build())

        Log.i(JournalOptions.TAG, "Notification Sent")
    }
}