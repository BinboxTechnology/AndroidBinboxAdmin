package com.binboxlockers.admin.ui.users

/**
 * @author Chris Byers 12/23/20 - Copyright 2020 Binbox Lockers
 */

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UsersViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text
}