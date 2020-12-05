package com.example.dailyjournal11

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.*


class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userName: TextView
    private lateinit var userID: TextView
    private lateinit var userPass: TextView
    private lateinit var userPassConfirmation: TextView
    private lateinit var errorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)

        // Initialize firebase
        mAuth = FirebaseAuth.getInstance()

        userName = findViewById(R.id.editTextTextName)
        userID = findViewById(R.id.editTextTextUserId)
        userPass = findViewById(R.id.editTextTextPassword)
        userPassConfirmation = findViewById(R.id.editTextTextPasswordConfirmation)
        errorTextView = findViewById(R.id.errorTextView)

        // Make it so hitting the enter key hits register button
        userPass.setOnKeyListener { v, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                register()
                true
            }
            false
        }

        // Attempt to register new user if clicked
        var registerButtonView = findViewById<Button>(R.id.registerButton)
        registerButtonView.setOnClickListener {
            register()
        }

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        checkLogin(currentUser)
    }

    // If user is already logged in, skip login page
    private fun checkLogin(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this@RegisterActivity, Journals::class.java)
            startActivity(intent)
        }
    }

    // Function to easily call createAccount()
    private fun register() {
        if (userPass.text.toString() != userPassConfirmation.text.toString()) {
            Toast.makeText(
                this@RegisterActivity,
                "Passwords don't match", Toast.LENGTH_LONG
            ).show()
        } else {
            createAccount(
                userName.text.toString(),
                userID.text.toString(),
                userPass.text.toString()
            )
        }
    }

    private fun createAccount(name: String, email: String, password: String) {
        // Check that boxes were filled
        if (name != "" && email != "" && password != "") {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")

                        // Include a name in the user's profile
                        var changeRequest =
                            UserProfileChangeRequest.Builder().setDisplayName(name).build()
                        mAuth.currentUser?.updateProfile(changeRequest)

                        val user = mAuth.currentUser
                        sendVerification(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        val localizedMessage = task.exception!!.localizedMessage
                        errorTextView.text = "Error: $localizedMessage"
                    }

                }
        } else {
            // If user forgets to enter name/username/password
            Toast.makeText(
                this@RegisterActivity,
                "Please enter a name, username, and password", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun sendVerification(user: FirebaseUser?) {
        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.w(TAG, "email verification: success", task.exception)
                    FirebaseAuth.getInstance().signOut()
                    errorTextView.setTextColor(Color.BLACK)
                    errorTextView.text = "Email verification sent! Check your email to verify account"
                } else {
                    Log.w(TAG, "email verification: failure", task.exception)
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error sending verification email", Toast.LENGTH_LONG
                    ).show()
                }
            }

    }


    companion object {
        const val TAG = "DailyJournalTAG"
    }

}