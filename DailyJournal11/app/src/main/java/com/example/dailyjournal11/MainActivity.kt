package com.example.dailyjournal11

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var registerButtonView = findViewById<Button>(R.id.registerButton)
        registerButtonView.setOnClickListener {
            Toast.makeText(applicationContext, "clicked register", Toast.LENGTH_SHORT).show() //TODO remove this
        }

        var loginButtonView = findViewById<Button>(R.id.loginButton)
        loginButtonView.setOnClickListener {
            Toast.makeText(applicationContext, "clicked login", Toast.LENGTH_SHORT).show() //TODO remove this
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
        val TAG = "DailyJournalTAG"
    }

}