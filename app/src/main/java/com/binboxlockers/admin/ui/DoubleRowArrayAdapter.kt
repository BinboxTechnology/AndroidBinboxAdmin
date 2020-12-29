package com.binboxlockers.admin.ui

/**
 * @author Chris Byers 12/22/20 - Copyright 2020 Binbox Lockers
 */

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.binboxlockers.admin.R

interface DoubleRowItem {
    fun getTextRow1(): String
    fun getTextRow2(): String
}

const val ITEM_LAYOUT = R.layout.double_row_spinner_item
const val DROPDOWN_ITEM_LAYOUT = R.layout.double_row_spinner_dropdown_item

class DoubleRowArrayAdapter(context: Context, private val items: List<DoubleRowItem>):
    ArrayAdapter<DoubleRowItem>(context, -1, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, ITEM_LAYOUT)
    }
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(position, convertView, parent, DROPDOWN_ITEM_LAYOUT)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup?, resource_id: Int): View{
        val view: LinearLayout = LayoutInflater.from(context).inflate(resource_id, parent, false) as LinearLayout
        val text1: TextView = view.findViewById(R.id.text1)
        val text2: TextView = view.findViewById(R.id.text2)
        text1.text = items[position].getTextRow1()
        text2.text = items[position].getTextRow2()

        return view
    }
}