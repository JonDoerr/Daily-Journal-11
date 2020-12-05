package com.example.dailyjournal11


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class JournalOptions : AppCompatActivity() {

    private lateinit var mContext: Context
    private lateinit var notificationText: TextView
    private lateinit var offButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.options_layout)

        // Set notification text and buttons
        notificationText = findViewById<Switch>(R.id.onOffTextview)
        offButton = findViewById(R.id.offButton)
        deleteAccountButton = findViewById(R.id.delete_account_button)
        mContext = this

        mAuth = FirebaseAuth.getInstance()

        // If user hits the off button, cancel their notification
        offButton.setOnClickListener {
            cancelNotification()
            notificationText.text = "Off"
        }

        // Give users options to delete their account
        deleteAccountButton.setOnClickListener {
            // From https://stackoverflow.com/questions/59340099/how-to-set-confirm-delete-alertdialogue-box-in-kotlin
            val builder = AlertDialog.Builder(this@JournalOptions)
            builder.setMessage("Are you sure you want to delete your account?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    deleteAccount()
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }
    }

    // Make sure On/Off text is set properly
    override fun onResume() {
        super.onResume()
        if (hasAlarm())
            setOn()
    }

    fun setOn() {
        notificationText.text = "On"
    }

    // For selecting a time
    fun showTimePickerDialog(v: View) {
        Log.i(TAG, "Entered time picker")
        TimePickerFragment(mContext).show(supportFragmentManager, "timePicker")
    }

    // Cancel notification alarm
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
        Log.i(TAG, "Canceled notification")
    }

    // Check to see if an alarm has been set
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

    private fun deleteAccount() {
        var user = mAuth.currentUser

        user!!.delete()
            .addOnCompleteListener(this) {
                task ->
            if (task.isSuccessful) {
                Log.w(RegisterActivity.TAG, "delete user success", task.exception)
                Toast.makeText(
                    this@JournalOptions,
                    "Account Deleted", Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this@JournalOptions, MainActivity::class.java)
                startActivity(intent)
            } else {
                // If account deletion fails, display a message to the user.
                Log.w(RegisterActivity.TAG, "delete user failure", task.exception)
                val localizedMessage = task.exception!!.localizedMessage
                Toast.makeText(
                    this@JournalOptions,
                    "Error: $localizedMessage", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        var CHANNEL_ID = "Daily Reminder"
        const val TAG = "DailyJournalTAG"
    }

}