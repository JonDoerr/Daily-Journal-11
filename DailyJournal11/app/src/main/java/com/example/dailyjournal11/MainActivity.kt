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
    private lateinit var passTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        // Initialize firebase
        mAuth = FirebaseAuth.getInstance()

        userID = findViewById(R.id.editTextTextUserId)
        userPass = findViewById(R.id.editTextTextPassword)
        errorTextView = findViewById(R.id.errorTextView)
        passTextView = findViewById(R.id.editTextTextPassword)

        // Make it so hitting the enter key logs in
        passTextView.setOnKeyListener { v, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                login()
                true
            }
            false
        }

        var registerButtonView = findViewById<Button>(R.id.registerButton)
        registerButtonView.setOnClickListener {
//            Toast.makeText(applicationContext, "clicked register", Toast.LENGTH_SHORT).show() //TODO remove this
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        var loginButtonView = findViewById<Button>(R.id.loginButton)
        loginButtonView.setOnClickListener {
//            Toast.makeText(applicationContext, "clicked login", Toast.LENGTH_SHORT).show() //TODO remove this
            login()
        }
    }

    private fun login() {
        signIn(userID.text.toString(), userPass.text.toString())
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
            val intent = Intent(this@MainActivity, Journals::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(applicationContext, "not logged in", Toast.LENGTH_SHORT).show() //TODO remove this
        }
    }

    private fun signIn(email: String, password: String) {
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
//                        val error = task.exception!! as FirebaseAuthException
//                        val errorCode = error.errorCode

                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        errorTextView.text = "Error: $localizedMessage"

                        checkLogin(null)
                    }

                    // ...
                }
        } else {
            Toast.makeText(
                this@MainActivity,
                "Please enter a username and password", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createAccount(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = mAuth.currentUser
                    checkLogin(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        this@MainActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    checkLogin(null)
                }

                // ...
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