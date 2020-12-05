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


class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var userID: TextView
    private lateinit var userPass: TextView
    private lateinit var errorTextView: TextView
    private lateinit var resetPassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        // Initialize firebase
        mAuth = FirebaseAuth.getInstance()

        userID = findViewById(R.id.editTextTextUserId)
        userPass = findViewById(R.id.editTextTextPassword)
        errorTextView = findViewById(R.id.errorTextView)
        resetPassword = findViewById(R.id.resetPasswordButton)

        // Make it so hitting the enter key logs in
        userPass.setOnKeyListener { v, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                login()
                true
            }
            false
        }

        // If user wants to register an account, open register activity
        var registerButtonView = findViewById<Button>(R.id.registerButton)
        registerButtonView.setOnClickListener {
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Attempt to log in if user hits login button
        var loginButtonView = findViewById<Button>(R.id.loginButton)
        loginButtonView.setOnClickListener {
            login()
        }

        resetPassword.setOnClickListener {
            val intent = Intent(this@MainActivity, PasswordResetActivity::class.java)
            startActivity(intent)
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
            if (user.isEmailVerified) {
                Log.d(TAG, "email verified and user signed in")
                val intent = Intent(this@MainActivity, Journals::class.java).putExtra("username", user!!.email!!.replace(".", "%",true))
                startActivity(intent)
            } else {
                Log.d(TAG, "email not verified")
                Toast.makeText(
                    this@MainActivity,
                    "Email not verified, resending email", Toast.LENGTH_LONG
                ).show()
                user.reload()
                user.sendEmailVerification()
            }
        }
    }

    private fun login() {
        // Function to easily call signIn()
        signIn(userID.text.toString(), userPass.text.toString())
    }

    private fun signIn(email: String, password: String) {
        // Check that boxes were filled
        if (email != "" && password != "") {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = mAuth.currentUser
                        checkLogin(user)
                    } else {
                        val localizedMessage = task.exception!!.localizedMessage

                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        errorTextView.text = "Error: $localizedMessage"
                    }
                }
        } else {
            // If user forgets to enter username/password
            Toast.makeText(
                this@MainActivity,
                "Please enter a username and password", Toast.LENGTH_LONG
            ).show()
        }
    }


    /**
     * For the options menu in the top right part of the screen. Nothing additional needs to be done
     * with this
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu)
        return true
    }

    /**
     * For the options menu in the top right part of the screen. Nothing additional needs to be done
     * with this
     */
    fun displayDialog(item: MenuItem) {
        Log.i(TAG, "displaying dialog box")
        class DialogFrag : DialogFragment() {
            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                val builder = AlertDialog.Builder(this.context)
                builder.setMessage(R.string.menu_content)
                builder.setTitle(R.string.menu_name)
                return builder.create()
            }
        }
        val frag = DialogFrag()
        frag.show(supportFragmentManager, "tag")
    }

    companion object {
        const val TAG = "DailyJournalTAG"
    }

}