/**
 * This file is from Lab 5 Lifecycle Aware
 */

package com.example.dailyjournal11
import java.util.ArrayList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class HistoryListAdapter (private val mContext: Context) : BaseAdapter()
{
    private var history : MutableList<String> = ArrayList()
    override fun getCount(): Int {
        return history.size
    }

    fun setHistory(newHistory: MutableList<String>){
        history =  newHistory
    }

    override fun getItemId(position: Int): Long {
        return  position.toLong()
    }

    override fun getItem(position: Int): Any? {
        return history[position]
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val layoutInflater = LayoutInflater.from(mContext).inflate(R.layout.history_list,parent,false)
        val txt = layoutInflater.findViewById<TextView>(R.id.Text)
        txt.text = history[position]
        return txt
    }
}