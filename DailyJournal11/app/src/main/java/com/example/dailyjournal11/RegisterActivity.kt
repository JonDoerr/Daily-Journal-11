package com.example.dailyjournal11

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.*


class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userName: TextView
    private lateinit var userID: TextView
    private lateinit var userPass: TextView
    private lateinit var errorTextView: TextView
    private lateinit var passTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)

        // Initialize firebase
        mAuth = FirebaseAuth.getInstance()

        userName = findViewById(R.id.editTextTextName)
        userID = findViewById(R.id.editTextTextUserId)
        userPass = findViewById(R.id.editTextTextPassword)
        errorTextView = findViewById(R.id.errorTextView)
        passTextView = findViewById(R.id.editTextTextPassword)

        // Make it so hitting the enter key hits register button
        passTextView.setOnKeyListener { v, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                register()
                true
            }
            false
        }

        var registerButtonView = findViewById<Button>(R.id.registerButton)
        registerButtonView.setOnClickListener {
//            Toast.makeText(applicationContext, "clicked register", Toast.LENGTH_SHORT).show() //TODO remove this
            register()
        }

    }

    private fun register() {
        createAccount(userName.text.toString(), userID.text.toString(), userPass.text.toString())
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        checkLogin(currentUser)
    }

    private fun checkLogin(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(applicationContext, "logged in", Toast.LENGTH_SHORT).show() //TODO remove this
            val intent = Intent(this@RegisterActivity, Journals::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(applicationContext, "not logged in", Toast.LENGTH_SHORT).show() //TODO remove this
        }
    }

    private fun createAccount(name: String, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")

                    var changeRequest = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                    mAuth.currentUser?.updateProfile(changeRequest)

                    val user = mAuth.currentUser
                    checkLogin(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    val localizedMessage = task.exception!!.localizedMessage
                    errorTextView.text = "Error: $localizedMessage"
                    checkLogin(null)
                }

            }
    }


    companion object {
        const val TAG = "DailyJournalTAG"
    }

}