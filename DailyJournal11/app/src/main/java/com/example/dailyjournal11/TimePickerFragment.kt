package com.example.dailyjournal11

import android.app.*
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*


class TimePickerFragment(c: Context) : DialogFragment(), OnTimeSetListener {

    private val mContext = c as JournalOptions

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c: Calendar = Calendar.getInstance()
        val hour: Int = c.get(Calendar.HOUR_OF_DAY)
        val minute: Int = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
        createNotificationChannel()
        sendNotification(hourOfDay, minute)
        mContext.setSwitch(true)
    }

    private fun sendNotification(hour: Int, minute: Int) {
        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(mContext.applicationContext, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarm = Calendar.getInstance(Locale.getDefault())
        alarm.timeInMillis = System.currentTimeMillis()
        alarm.set(Calendar.HOUR_OF_DAY, hour)
        alarm.set(Calendar.MINUTE, minute)
        alarm.set(Calendar.SECOND, 0)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            alarm.timeInMillis, AlarmManager.INTERVAL_DAY, alarmIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminder" // getString(R.string.channel_name)
            val descriptionText = "Daily reminder to record journal entry" // getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(JournalOptions.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}