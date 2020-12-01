package com.example.dailyjournal11


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity


class JournalOptions : AppCompatActivity() {

    private lateinit var mContext: Context
    private lateinit var notificationSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.options_layout)
        notificationSwitch = findViewById<Switch>(R.id.notification_switch)
        mContext = this

        setSwitch()

        notificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                notificationSwitch.text = "On"
            else {
                notificationSwitch.text = "Off"
                cancelNotification()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setSwitch()
        notificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                notificationSwitch.text = "On"
            else {
                notificationSwitch.text = "Off"
                cancelNotification()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setSwitch()
        notificationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                notificationSwitch.text = "On"
            else {
                notificationSwitch.text = "Off"
                cancelNotification()
            }
        }
    }

    private fun setSwitch() {
        if (hasAlarm()) {
            notificationSwitch.isChecked = true
            notificationSwitch.text = "On"
        }
        else {
            notificationSwitch.isChecked = false
            notificationSwitch.text = "Off"
        }
    }

    fun setSwitch(b: Boolean) {
        notificationSwitch.isChecked = b
    }

    fun showTimePickerDialog(v: View) {
        TimePickerFragment(mContext).show(supportFragmentManager, "timePicker")
    }

    private fun cancelNotification() {
        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(mContext.applicationContext, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(
            mContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(alarmIntent)
    }

    private fun hasAlarm(): Boolean {
        val intent = Intent(mContext.applicationContext, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(
            mContext,
            0,
            intent,
            PendingIntent.FLAG_NO_CREATE // returns null if alarm doesn't exist
        )

        return alarmIntent != null
    }

    companion object {
        var CHANNEL_ID = "Daily Reminder"
    }

}