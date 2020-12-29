package com.binboxlockers.admin.ui.lockers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.binboxlockers.admin.data.LoginDataSource
import com.binboxlockers.admin.data.LoginRepository
import com.binboxlockers.admin.data.ParseDataSource
import com.binboxlockers.admin.data.ParseRepository

/**
 * @author Chris Byers 12/22/20 - Copyright 2020 Binbox Lockers
 *
 * ViewModel provider factory to instantiate LockersViewModel.
 * Required given LockersViewModel has a non-empty constructor
 */
class LockersViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LockersViewModel::class.java)) {
            return LockersViewModel(
                parseRepository = ParseRepository(
                    dataSource = ParseDataSource()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}