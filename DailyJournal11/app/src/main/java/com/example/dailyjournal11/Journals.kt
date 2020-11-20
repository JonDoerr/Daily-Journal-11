package com.example.dailyjournal11

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

private lateinit var mNewJournalButton: Button
private lateinit var mListView: ListView
private lateinit var journalList: ArrayList<String>
private lateinit var mAdapter: HistoryListAdapter
private lateinit var mSortButton: Button

class Journals : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        Log.i(TAG, "onActivityResult")
        if(resultCode == 436) {
            var body = data.getStringExtra("body")
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

    companion object {
        var TAG = "Journals"
        val GET_BODY_REQUEST_CODE = 123
    }
}