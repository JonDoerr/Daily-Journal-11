package com.example.dailyjournal11

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.*


class PasswordResetActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userID: TextView
    private lateinit var errorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset_layout)

        // Initialize firebase
        mAuth = FirebaseAuth.getInstance()

        userID = findViewById(R.id.editTextTextUserId)
        errorTextView = findViewById(R.id.errorTextView)

        // Attempt to register new user if clicked
        var resetButtonView = findViewById<Button>(R.id.resetButton)
        resetButtonView.setOnClickListener {
            resetPassword()
        }

    }

    // Function to easily call reset()
    private fun resetPassword() {
        reset(userID.text.toString())
    }

    private fun reset(email: String) {
        // Check that boxes were filled
        if (email != "") {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this) {
                task ->
                if (task.isSuccessful) {
                    errorTextView.setTextColor(Color.BLACK)
                    errorTextView.text = "Reset email sent!"
                } else {
                    // If password reset fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    val localizedMessage = task.exception!!.localizedMessage
                    errorTextView.text = "Error: $localizedMessage"
                }
            }
        } else {
            // If user forgets to enter email
            Toast.makeText(
                this@PasswordResetActivity,
                "Please enter an email", Toast.LENGTH_LONG
            ).show()
        }
    }


    companion object {
        const val TAG = "DailyJournalTAG"
    }

}