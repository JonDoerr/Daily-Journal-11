/**
 * This file is from Lab 5 Lifecycle Aware
 */

package com.example.dailyjournal11
import android.annotation.SuppressLint
import android.app.Activity
import java.util.ArrayList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class HistoryListAdapter (private val context: Activity, private var journals: List<JournalData>) : ArrayAdapter<JournalData>(context, R.layout.history_list, journals)
{

//gets the view of the history list adapter for displaying journals in the main layout
    //this code is based off of the code given to use in lab7- firebase and modified to refelct what we need it to do in our project
    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.history_list, null, true)

        val textViewText = listViewItem.findViewById<View>(R.id.list_text_view) as TextView

        val journal = journals[position]
        textViewText.text = journal.journalDate

        return listViewItem
    }
}