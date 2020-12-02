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

    //TODO- remove this at end. hold onto for now if we do need any of it
//    private var history : MutableList<JournalData> = ArrayList()
//    override fun getCount(): Int {
//        return history.size
//    }
//
//    fun setHistory(newHistory: MutableList<JournalData>){
//        history =  newHistory
//    }
//
//    override fun getItemId(position: Int): Long {
//        return  position.toLong()
//    }
//
//    override fun getItem(position: Int): JournalData? {
//        return history[position]
//    }

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