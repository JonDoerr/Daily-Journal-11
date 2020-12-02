package com.example.dailyjournal11


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class JournalOptions : AppCompatActivity() {

    private lateinit var mContext: Context
    private lateinit var notificationText: TextView
    private lateinit var offButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.options_layout)
        notificationText = findViewById<Switch>(R.id.onOffTextview)
        offButton = findViewById(R.id.offButton)
        mContext = this

        offButton.setOnClickListener {
            cancelNotification()
            notificationText.text = "Off"
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasAlarm())
            setOn()
    }

    fun setOn() {
        notificationText.text = "On"
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