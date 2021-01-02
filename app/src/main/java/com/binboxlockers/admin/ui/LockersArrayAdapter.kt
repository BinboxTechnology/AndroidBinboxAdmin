package com.binboxlockers.admin.ui

/**
 * @author Chris Byers 12/22/20 - Copyright 2020 Binbox Lockers
 */

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.LayoutRes
import com.binboxlockers.admin.R
import com.binboxlockers.admin.data.model.Locker

class LockersArrayAdapter(context: Context, private val items: List<Locker>, clickHandler: (locker: Locker) -> Unit,
            personClickHandler: (locker: Locker) -> Unit):
    ArrayAdapter<Locker>(context, -1, items) {

    private val _clickHandler = clickHandler
    private val _personClickHandler = personClickHandler

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, R.layout.locker_list_item)
    }
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(position, convertView, parent, R.layout.locker_list_item)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup?, resource_id: Int): View{
        val view: RelativeLayout = LayoutInflater.from(context).inflate(resource_id, parent, false) as RelativeLayout
        val text1: TextView = view.findViewById(R.id.text_locker_number)
        val unlockBtn: Button = view.findViewById(R.id.btn_open_locker)
        text1.text = items[position].userLockerNumber
        unlockBtn.setOnClickListener {
            _clickHandler(items[position])
        }
        val personIcon = view.findViewById<ImageButton>(R.id.btn_user_rented)
        personIcon.visibility = if (items[position].user.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        personIcon.setOnClickListener {
            _personClickHandler(items[position])
        }

        return view
    }
}