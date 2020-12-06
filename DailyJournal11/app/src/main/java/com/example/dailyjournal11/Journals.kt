package com.example.dailyjournal11

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private lateinit var mNewJournalButton: Button
private lateinit var mListView: ListView
private lateinit var journalListView: ListView
private lateinit var mAdapter: HistoryListAdapter
private lateinit var mSortButton: Button

internal lateinit var journals: MutableList<JournalData>
private lateinit var databaseJournals: DatabaseReference
private lateinit var uid: String

private lateinit var mDate: LocalDateTime

class Journals : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        journals = ArrayList()
        uid = intent.getStringExtra("username")!!
        databaseJournals = FirebaseDatabase.getInstance().getReference("journals/$uid")
        journalListView = findViewById(R.id.journals)
        mNewJournalButton = findViewById(R.id.newJournalButton)

        mNewJournalButton.setOnClickListener {
            Log.i(TAG, "newJournal Button clicked")
            newJournal()
        }

        mSortButton = findViewById(R.id.SortButton)
        mSortButton.setOnClickListener {
            Log.i(TAG, "sort Button clicked")

            if(mSortButton.text == "ascending") {
                mSortButton.text = "descending"
            }
            else {
                mSortButton.text = "ascending"
            }
            journals = journals.reversed().toMutableList()
            journalListView.adapter = HistoryListAdapter(this@Journals, journals)
        }

        mDate = LocalDateTime.now()

        journalListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

            val journal = journals[i]

            val existingJournalIntent = Intent(applicationContext, JournalDesign::class.java).putExtra("username", uid)

            existingJournalIntent.putExtra("DATE", journal.journalDate)
            existingJournalIntent.putExtra("ID", journal.journalId)
            existingJournalIntent.putExtra("TEXT", journal.journalText)

            existingJournalIntent.putExtra("ISNEWJOURNAL", false)

            startActivityForResult(existingJournalIntent, GET_BODY_REQUEST_CODE)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult")
        if(resultCode == 436) {
            Log.i(TAG, "Journal submitted sucessfully")
        }
    }

    //Go to the journal activity
    private fun newJournal() {
        mDate = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatedDate = mDate.format(formatter)

        //make a call to the activity that lets the user create a journal
        val intent = Intent(this@Journals, JournalDesign::class.java).putExtra("username", uid)

        intent.putExtra("DATE", formatedDate)
        intent.putExtra("ISNEWJOURNAL", true)

        startActivityForResult(intent, GET_BODY_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.journal_menu, menu)
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        if (id == R.id.options) {
            val intent = Intent(this@Journals, JournalOptions::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@Journals, MainActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun displayDialog(item: MenuItem) {
        Log.i(Journals.TAG, "displaying dialog box")
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

    //displays the database list
    override fun onStart() {
        super.onStart()

        databaseJournals.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                journals.clear()

                var journal: JournalData? = null
                for (postSnapshot in dataSnapshot.children) {
                    try {
                        journal = postSnapshot.getValue(JournalData::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, e.toString())
                    } finally {
                        journals.add(journal!!)
                    }
                }
                if(mSortButton.text == "descending") {
                    journals = journals.reversed().toMutableList()
                }
                val journalAdapter = HistoryListAdapter(this@Journals, journals)
                journalListView.adapter = journalAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "cancelled")
            }
        })
    }

    companion object {
        var TAG = "Journals"
        val GET_BODY_REQUEST_CODE = 123
    }
}