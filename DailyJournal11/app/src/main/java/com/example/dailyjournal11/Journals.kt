package com.example.dailyjournal11

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

private lateinit var mNewJournalButton: Button
private lateinit var mListView: ListView
private lateinit var journalList: ArrayList<String>
private lateinit var mAdapter: HistoryListAdapter
private lateinit var mSortButton: Button

internal lateinit var journals: MutableList<JournalData>
private lateinit var databaseJournals: DatabaseReference
private lateinit var uid: String

class Journals : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        //TODO- for creating list of journals from the database
//        databaseJournals = FirebaseDatabase.getInstance().getReference("authors")
//        journals = ArrayList()
//        uid = intent.getStringExtra(USER_ID)!!


        journalList = arrayListOf<String>()
        mNewJournalButton = findViewById(R.id.newJournalButton)
        mAdapter = HistoryListAdapter(applicationContext)
        mNewJournalButton.setOnClickListener {
            Toast.makeText(applicationContext, "newJournal Button clicked", Toast.LENGTH_SHORT).show() //TODO remove this
            newJournal()
        }

        mSortButton = findViewById(R.id.SortButton)
        mSortButton.setOnClickListener {
            Toast.makeText(applicationContext, "sort Button clicked", Toast.LENGTH_SHORT).show() //TODO remove this
        }


        var list = findViewById<ListView>(R.id.journals)
        list.adapter = mAdapter
        mAdapter.setHistory(journalList)

        journalList.add("436")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult")
        if(resultCode == 436) {
            var body = data?.getStringExtra("body")
            journalList!!.add(body!!)
            mAdapter.notifyDataSetChanged()
        }
    }

    //Go to the journal activity
    //TODO entry's don't persist (they will either need to be put in storage or on firebase)
    private fun newJournal() {
        //journalList.add("asdf")
        //mAdapter.notifyDataSetChanged()
        //TODO make a call to the activity that lets the user create a journal
        val intent = Intent(this@Journals, JournalDesign::class.java)
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


    //TODO - for the database list we need to use something like this
    // for creating list
//    override fun onStart() {
//        super.onStart()
//
//        databaseAuthors.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                authors.clear()
//
//                var author: Author? = null
//                for (postSnapshot in dataSnapshot.child(uid).children) {
//                    try {
//                        author = postSnapshot.getValue(Author::class.java)
//                    } catch (e: Exception) {
//                        Log.e(TAG, e.toString())
//                    } finally {
//                        authors.add(author!!)
//                    }
//                }
//
//                val authorAdapter = AuthorList(this@DashboardActivity, authors)
//                listViewAuthors.adapter = authorAdapter
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//
//            }
//        })
//    }


    companion object {
        var TAG = "Journals"
        val GET_BODY_REQUEST_CODE = 123
    }


}